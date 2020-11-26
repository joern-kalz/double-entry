package com.github.joern.kalz.doubleentry.services.transactions;

import com.github.joern.kalz.doubleentry.models.Transaction;
import com.github.joern.kalz.doubleentry.models.TransactionSearchCriteria;
import com.github.joern.kalz.doubleentry.models.TransactionsRepository;
import com.github.joern.kalz.doubleentry.services.PrincipalProvider;
import com.github.joern.kalz.doubleentry.services.accounts.AccountsHierarchyService;
import com.github.joern.kalz.doubleentry.services.exceptions.NotFoundException;
import com.github.joern.kalz.doubleentry.services.exceptions.ParameterException;
import com.github.joern.kalz.doubleentry.services.transactions.TransactionsValidator.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class TransactionsService {

    @Autowired
    private TransactionsRepository transactionsRepository;

    @Autowired
    private PrincipalProvider principalProvider;

    @Autowired
    private TransactionsConverter transactionsConverter;

    @Autowired
    private TransactionsValidator transactionsValidator;

    @Autowired
    private AccountsHierarchyService accountsHierarchyService;

    public List<Transaction> find(FindTransactionsRequest request) {

        TransactionSearchCriteria criteria = new TransactionSearchCriteria();
        criteria.setUser(principalProvider.getPrincipal());

        if (request.getAccountId() != null) {
            Set<Long> accountsSet = accountsHierarchyService.getChildrenById(request.getAccountId()).keySet();
            ArrayList<Long> accounts = new ArrayList<>(accountsSet);
            accounts.add(request.getAccountId());
            criteria.setAccountIds(accounts);
        }

        criteria.setAfter(request.getAfter());
        criteria.setBefore(request.getBefore());
        criteria.setName(request.getName());
        criteria.setPageOffset(request.getPageOffset());
        criteria.setMaxPageSize(request.getMaxPageSize());
        criteria.setOrder(request.getOrder());

        return transactionsRepository.find(criteria);
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
