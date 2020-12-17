package com.github.joern.kalz.doubleentry.services.repository;

import com.github.joern.kalz.doubleentry.models.*;
import com.github.joern.kalz.doubleentry.services.PrincipalProvider;
import com.github.joern.kalz.doubleentry.services.exceptions.ParameterException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RepositoryService {

    private final AccountsRepository accountsRepository;
    private final TransactionsRepository transactionsRepository;
    private final PrincipalProvider principalProvider;

    public RepositoryService(AccountsRepository accountsRepository, TransactionsRepository transactionsRepository,
                             PrincipalProvider principalProvider) {
        this.accountsRepository = accountsRepository;
        this.transactionsRepository = transactionsRepository;
        this.principalProvider = principalProvider;
    }

    @Transactional
    public GetRepositoryResponse getRepository() {
        User principal = principalProvider.getPrincipal();

        GetRepositoryResponse getRepositoryResponse = new GetRepositoryResponse();
        getRepositoryResponse.setAccounts(accountsRepository.findByUser(principal));
        getRepositoryResponse.setTransactions(transactionsRepository.findByUser(principal));

        return getRepositoryResponse;
    }

    @Transactional
    public void importRepository(ImportRepositoryRequest importRepositoryRequest) {
        importRepository(principalProvider.getPrincipal(), importRepositoryRequest);
    }

    @Transactional
    public void importRepository(User user, ImportRepositoryRequest importRepositoryRequest) {
        Map<Long, Account> databaseAccountsByImportId = importAccounts(user, importRepositoryRequest.getAccounts());
        importTransactions(user, importRepositoryRequest.getTransactions(), databaseAccountsByImportId);
    }

    private Map<Long, Account> importAccounts(User user, List<ImportRepositoryRequestAccount> importAccounts) {
        Map<Long, List<ImportRepositoryRequestAccount>> importAccountsByParentId = importAccounts.stream()
                .filter(importAccount -> importAccount.getParentId() != null)
                .collect(Collectors.groupingBy(ImportRepositoryRequestAccount::getParentId));
        Deque<ImportRepositoryRequestAccount> toBeAdded = new ArrayDeque<>(getRootAccounts(importAccounts));
        Map<Long, Account> databaseAccountsByImportId = new HashMap<>();

        while (!toBeAdded.isEmpty()) {
            ImportRepositoryRequestAccount importAccount = toBeAdded.removeFirst();

            if (databaseAccountsByImportId.containsKey(importAccount.getId())) {
                throw new ParameterException("cyclic parent child relationship in account list");
            }

            Account databaseAccount = createAccount(user, importAccount, databaseAccountsByImportId);
            databaseAccountsByImportId.put(importAccount.getId(), databaseAccount);
            toBeAdded.addAll(getChildren(importAccount, importAccountsByParentId));
        }

        List<ImportRepositoryRequestAccount> missingAccounts = getAccountsMissingInDatabase(importAccounts,
                databaseAccountsByImportId);

        if (!missingAccounts.isEmpty()) {
            throw new ParameterException("parentId not valid for the accounts " + missingAccounts);
        }

        return databaseAccountsByImportId;
    }

    private List<ImportRepositoryRequestAccount> getRootAccounts(List<ImportRepositoryRequestAccount> importAccounts) {
        List<ImportRepositoryRequestAccount> rootAccounts = importAccounts.stream()
                .filter(account -> account.getParentId() == null)
                .collect(Collectors.toList());

        if (rootAccounts.isEmpty()) {
            throw new ParameterException("did not find any root account in repository");
        }

        return rootAccounts;
    }

    private Account createAccount(User user, ImportRepositoryRequestAccount importAccount,
              Map<Long, Account> databaseAccountsByImportId) {
        Account parentDatabaseAccount = databaseAccountsByImportId.get(importAccount.getParentId());

        Account databaseAccount = new Account();
        databaseAccount.setUser(user);
        databaseAccount.setName(importAccount.getName());
        databaseAccount.setActive(importAccount.isActive());
        databaseAccount.setParent(parentDatabaseAccount);

        databaseAccount = accountsRepository.save(databaseAccount);
        return databaseAccount;
    }

    private List<ImportRepositoryRequestAccount> getChildren(ImportRepositoryRequestAccount importAccount,
             Map<Long, List<ImportRepositoryRequestAccount>> importAccountsByParentId) {
        return importAccountsByParentId.getOrDefault(importAccount.getId(), Collections.emptyList());
    }

    private List<ImportRepositoryRequestAccount> getAccountsMissingInDatabase(
            List<ImportRepositoryRequestAccount> importAccounts, Map<Long, Account> databaseAccountsByImportId) {
        return importAccounts.stream()
                .filter(importAccount -> !databaseAccountsByImportId.containsKey(importAccount.getId()))
                .collect(Collectors.toList());
    }

    private void importTransactions(User user, List<ImportRepositoryRequestTransaction> importTransactions,
                                    Map<Long, Account> databaseAccountsByImportId) {
        for (ImportRepositoryRequestTransaction importTransaction : importTransactions) {
            Transaction transaction = new Transaction();
            transaction.setUser(user);
            transaction.setDate(importTransaction.getDate());
            transaction.setName(importTransaction.getName());
            transaction.setEntries(importTransaction.getEntries().stream()
                    .map(entry -> convertToEntryOfTransaction(entry, transaction, databaseAccountsByImportId))
                    .collect(Collectors.toList()));

            transactionsRepository.save(transaction);
        }
    }

    private Entry convertToEntryOfTransaction(ImportRepositoryRequestEntry importEntry, Transaction transaction,
                                              Map<Long, Account> databaseAccountsByImportId) {
        Account account = databaseAccountsByImportId.get(importEntry.getAccountId());

        if (account == null) {
            throw new ParameterException("account " + importEntry.getAccountId() + " not defined");
        }

        EntryId entryId = new EntryId();
        entryId.setAccount(account);
        entryId.setTransaction(transaction);

        Entry entry = new Entry();
        entry.setId(entryId);
        entry.setAmount(importEntry.getAmount());
        entry.setVerified(importEntry.isVerified());

        return entry;
    }
}
