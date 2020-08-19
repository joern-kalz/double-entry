package com.github.joern.kalz.doubleentry.services.transaction;

import com.github.joern.kalz.doubleentry.controllers.exceptions.NotFoundException;
import com.github.joern.kalz.doubleentry.controllers.exceptions.ParameterException;
import com.github.joern.kalz.doubleentry.model.Transaction;
import com.github.joern.kalz.doubleentry.model.TransactionsRepository;
import com.github.joern.kalz.doubleentry.model.User;
import com.github.joern.kalz.doubleentry.services.PrincipalProvider;
import com.github.joern.kalz.doubleentry.services.transaction.TransactionValidator.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    @Autowired
    private TransactionsRepository transactionsRepository;

    @Autowired
    private PrincipalProvider principalProvider;

    @Autowired
    private TransactionConverter transactionConverter;

    @Autowired
    private TransactionValidator transactionValidator;

    public List<Transaction> findAll() {
        User principal = principalProvider.getPrincipal();
        return transactionsRepository.findByUser(principal);
    }

    public Transaction findById(long id) {
        return findTransaction(id)
                .orElseThrow(() -> new NotFoundException("transaction " + id + " not found"));
    }

    @Transactional
    public Transaction create(CreateTransactionRequest createRequest) {
        Transaction transaction = transactionConverter.convertToTransaction(createRequest);

        Result validationResult = transactionValidator.validate(transaction);

        if (validationResult != Result.OK) {
            throw createValidationException(validationResult);
        }

        return transactionsRepository.save(transaction);
    }

    @Transactional
    public void update(UpdateTransactionRequest updateRequest) {
        Transaction transaction = transactionConverter.convertToTransaction(updateRequest);

        Result validationResult = transactionValidator.validate(transaction);

        if (validationResult != Result.OK) {
            throw createValidationException(validationResult);
        }

        findTransaction(transaction.getId())
                .orElseThrow(() -> new NotFoundException("transaction " + transaction.getId() + " not found"));

        transactionsRepository.save(transaction);
    }

    @Transactional
    public void delete(long id) {
        Transaction transaction = findTransaction(id)
                .orElseThrow(() -> new NotFoundException("transaction " + id + " not found"));

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
                throw new RuntimeException("unknown validation error " + result);
        }
    }

    private Optional<Transaction> findTransaction(Long transactionId) {
        Optional<Transaction> transaction = transactionsRepository.findById(transactionId);

        if (transaction.isEmpty() || !transaction.get().getUser().equals(principalProvider.getPrincipal())) {
            return Optional.empty();
        }

        return transaction;
    }

}
