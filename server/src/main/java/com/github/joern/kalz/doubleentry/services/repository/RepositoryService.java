package com.github.joern.kalz.doubleentry.services.repository;

import com.github.joern.kalz.doubleentry.models.*;
import com.github.joern.kalz.doubleentry.services.PrincipalProvider;
import com.github.joern.kalz.doubleentry.services.exceptions.ParameterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RepositoryService {
    @Autowired
    private AccountsRepository accountsRepository;

    @Autowired
    private TransactionsRepository transactionsRepository;

    @Autowired
    private PrincipalProvider principalProvider;

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
        Map<Long, Account> databaseAccountsByImportId = importAccounts(importRepositoryRequest.getAccounts());
        importTransactions(importRepositoryRequest.getTransactions(), databaseAccountsByImportId);
    }

    private Map<Long, Account> importAccounts(List<ImportRepositoryRequestAccount> importAccounts) {
        Map<Long, List<ImportRepositoryRequestAccount>> importAccountsByParentId = importAccounts.stream()
                .filter(importAccount -> importAccount.getParentId() != null)
                .collect(Collectors.groupingBy(ImportRepositoryRequestAccount::getParentId));
        Deque<ImportRepositoryRequestAccount> toBeAdded = new ArrayDeque<>(getRootAccounts(importAccounts));
        Map<Long, Account> databaseAccountsByImportId = new HashMap<>();

        while (toBeAdded.size() > 0) {
            ImportRepositoryRequestAccount importAccount = toBeAdded.removeFirst();

            if (databaseAccountsByImportId.containsKey(importAccount.getId())) {
                throw new ParameterException("cyclic parent child relationship in account list");
            }

            Account databaseAccount = createAccount(importAccount, databaseAccountsByImportId);
            databaseAccountsByImportId.put(importAccount.getId(), databaseAccount);
            toBeAdded.addAll(getChildren(importAccount, importAccountsByParentId));
        }

        List<ImportRepositoryRequestAccount> missingAccounts = getAccountsMissingInDatabase(importAccounts,
                databaseAccountsByImportId);

        if (missingAccounts.size() > 0) {
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

    private Account createAccount(ImportRepositoryRequestAccount importAccount,
              Map<Long, Account> databaseAccountsByImportId) {
        Account parentDatabaseAccount = databaseAccountsByImportId.get(importAccount.getParentId());

        Account databaseAccount = new Account();
        databaseAccount.setUser(principalProvider.getPrincipal());
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

    private void importTransactions(List<ImportRepositoryRequestTransaction> importTransactions,
                                    Map<Long, Account> databaseAccountsByImportId) {
        for (ImportRepositoryRequestTransaction importTransaction : importTransactions) {
            Transaction transaction = new Transaction();
            transaction.setUser(principalProvider.getPrincipal());
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

        return new Entry(transaction, account, importEntry.getAmount(), importEntry.isVerified());
    }
}
