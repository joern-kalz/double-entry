package com.github.joern.kalz.doubleentry.controllers;

import com.github.joern.kalz.doubleentry.controllers.exceptions.ParameterException;
import com.github.joern.kalz.doubleentry.generated.api.TransactionsApi;
import com.github.joern.kalz.doubleentry.generated.model.CreatedResponse;
import com.github.joern.kalz.doubleentry.generated.model.GetTransactionResponse;
import com.github.joern.kalz.doubleentry.generated.model.SaveTransactionRequest;
import com.github.joern.kalz.doubleentry.generated.model.SaveTransactionRequestEntries;
import com.github.joern.kalz.doubleentry.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
public class TransactionsApiImpl implements TransactionsApi {

    @Autowired
    private TransactionsRepository transactionsRepository;

    @Autowired
    private AccountsRepository accountsRepository;

    @Override
    public ResponseEntity<CreatedResponse> createTransaction(@Valid SaveTransactionRequest saveTransactionRequest) {
        Transaction transaction = new Transaction(saveTransactionRequest.getDate(), saveTransactionRequest.getName());

        for (SaveTransactionRequestEntries entry : saveTransactionRequest.getEntries()) {
            boolean verified = Optional.ofNullable(entry.getVerified()).orElse(false);
            Account account = accountsRepository.findById(entry.getAccountId())
                    .orElseThrow(() -> new ParameterException("account " + entry.getAccountId() + " not found"));

            transaction.getEntries().add(new Entry(transaction, account, entry.getAmount(), verified));
        }

        if (transaction.getEntries().size() < 2) {
            throw new ParameterException("transaction must have two or more entries");
        } else if (!isEachEntryAccountUnique(transaction)) {
            throw new ParameterException("each account must only be referenced in at most one entry");
        } else if (!isTotalZero(transaction)) {
            throw new ParameterException("transaction total must be zero");
        }

        transactionsRepository.save(transaction);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> deleteTransaction(Long transactionId) {
        return null;
    }

    @Override
    public ResponseEntity<GetTransactionResponse> getTransaction(Long transactionId) {
        return null;
    }

    @Override
    public ResponseEntity<List<GetTransactionResponse>> getTransactions(@Valid LocalDate after, @Valid LocalDate before, @Valid BigDecimal accountId) {
        return null;
    }

    @Override
    public ResponseEntity<Void> updateTransaction(Long transactionId, @Valid SaveTransactionRequest saveTransactionRequest) {
        return null;
    }

    private boolean isEachEntryAccountUnique(Transaction transaction) {
        Set<Long> accountIds = new HashSet<>();

        for (Entry entry : transaction.getEntries()) {
            Long accountId = entry.getId().getAccount().getId();

            if (accountIds.contains(accountId)) {
                return false;
            }

            accountIds.add(accountId);
        }

        return true;
    }

    private boolean isTotalZero(Transaction transaction) {
        return transaction.getEntries().stream()
                .map(Entry::getAmount)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO)
                .compareTo(BigDecimal.ZERO) == 0;
    }
}
