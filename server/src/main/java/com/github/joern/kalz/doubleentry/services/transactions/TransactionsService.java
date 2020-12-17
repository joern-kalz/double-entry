package com.github.joern.kalz.doubleentry.services.transactions;

import com.github.joern.kalz.doubleentry.models.Transaction;
import com.github.joern.kalz.doubleentry.models.TransactionSearchCriteria;
import com.github.joern.kalz.doubleentry.models.TransactionsRepository;
import com.github.joern.kalz.doubleentry.services.PrincipalProvider;
import com.github.joern.kalz.doubleentry.services.accounts.AccountsHierarchyService;
import com.github.joern.kalz.doubleentry.services.exceptions.NotFoundException;
import com.github.joern.kalz.doubleentry.services.exceptions.ParameterException;
import com.github.joern.kalz.doubleentry.services.transactions.TransactionsValidator.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class TransactionsService {

    private final TransactionsRepository transactionsRepository;
    private final PrincipalProvider principalProvider;
    private final TransactionsConverter transactionsConverter;
    private final TransactionsValidator transactionsValidator;
    private final AccountsHierarchyService accountsHierarchyService;

    public TransactionsService(TransactionsRepository transactionsRepository,
                               PrincipalProvider principalProvider,
                               TransactionsConverter transactionsConverter,
                               TransactionsValidator transactionsValidator,
                               AccountsHierarchyService accountsHierarchyService) {
        this.transactionsRepository = transactionsRepository;
        this.principalProvider = principalProvider;
        this.transactionsConverter = transactionsConverter;
        this.transactionsValidator = transactionsValidator;
        this.accountsHierarchyService = accountsHierarchyService;
    }

    public List<Transaction> find(FindTransactionsRequest request) {

        TransactionSearchCriteria criteria = new TransactionSearchCriteria();
        criteria.setUser(principalProvider.getPrincipal());

        if (request.getAccountId() != null) {
            criteria.setAccountIds(getIdsOfAccountAndChildren(request.getAccountId()));
        }

        if (request.getCreditAccountId() != null) {
            criteria.setCreditAccountIds(getIdsOfAccountAndChildren(request.getCreditAccountId()));
        }

        if (request.getDebitAccountId() != null) {
            criteria.setDebitAccountIds(getIdsOfAccountAndChildren(request.getDebitAccountId()));
        }

        criteria.setAfter(request.getAfter());
        criteria.setBefore(request.getBefore());
        criteria.setName(request.getName());
        criteria.setPageOffset(request.getPageOffset());
        criteria.setMaxPageSize(request.getMaxPageSize());
        criteria.setOrder(request.getOrder());

        return transactionsRepository.find(criteria);
    }

    private List<Long> getIdsOfAccountAndChildren(Long accountId) {
        Set<Long> accountsSet = accountsHierarchyService.getChildrenById(accountId).keySet();
        List<Long> accounts = new ArrayList<>(accountsSet);
        accounts.add(accountId);
        return accounts;
    }

    public Transaction findById(long id) {
        return findTransaction(id)
                .orElseThrow(() -> createNotFoundException(id));
    }

    @Transactional
    public Transaction create(CreateTransactionRequest createRequest) {
        Transaction transaction = transactionsConverter.convertToTransaction(createRequest);

        Result validationResult = transactionsValidator.validate(transaction);

        if (validationResult != Result.OK) {
            throw createValidationException(validationResult);
        }

        return transactionsRepository.save(transaction);
    }

    @Transactional
    public void update(UpdateTransactionRequest updateRequest) {
        Transaction transaction = transactionsConverter.convertToTransaction(updateRequest);

        Result validationResult = transactionsValidator.validate(transaction);

        if (validationResult != Result.OK) {
            throw createValidationException(validationResult);
        }

        if (findTransaction(transaction.getId()).isEmpty()) {
            throw createNotFoundException(transaction.getId());
        }

        transactionsRepository.save(transaction);
    }

    @Transactional
    public void delete(long id) {
        Transaction transaction = findTransaction(id)
                .orElseThrow(() -> createNotFoundException(id));

        transactionsRepository.delete(transaction);
    }

    private RuntimeException createValidationException(Result result) {
        switch (result) {
            case LESS_THAN_TWO_ENTRIES:
                throw new ParameterException("transaction must have two or more entries");
            case SAME_ACCOUNT_IN_TWO_OR_MORE_ENTRIES:
                throw new ParameterException("each account must only be referenced in at most one entry");
            case TOTAL_NOT_ZERO:
                throw new ParameterException("transaction total must be zero");
            default:
                throw new IllegalArgumentException("unknown validation error " + result);
        }
    }

    private Optional<Transaction> findTransaction(Long transactionId) {
        Optional<Transaction> transaction = transactionsRepository.findById(transactionId);

        if (transaction.isEmpty() || !transaction.get().getUser().equals(principalProvider.getPrincipal())) {
            return Optional.empty();
        }

        return transaction;
    }

    private NotFoundException createNotFoundException(long id) {
        return new NotFoundException("transaction " + id + " not found");
    }
}
