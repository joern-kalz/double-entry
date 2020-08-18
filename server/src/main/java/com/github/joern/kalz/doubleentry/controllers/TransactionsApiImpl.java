package com.github.joern.kalz.doubleentry.controllers;

import com.github.joern.kalz.doubleentry.controllers.exceptions.NotFoundException;
import com.github.joern.kalz.doubleentry.controllers.exceptions.ParameterException;
import com.github.joern.kalz.doubleentry.generated.api.TransactionsApi;
import com.github.joern.kalz.doubleentry.generated.model.*;
import com.github.joern.kalz.doubleentry.model.*;
import com.github.joern.kalz.doubleentry.services.PrincipalProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class TransactionsApiImpl implements TransactionsApi {

    @Autowired
    private TransactionsRepository transactionsRepository;

    @Autowired
    private AccountsRepository accountsRepository;

    @Autowired
    private PrincipalProvider principalProvider;

    @Override
    @Transactional
    public ResponseEntity<CreatedResponse> createTransaction(@Valid SaveTransactionRequest saveTransactionRequest) {
        Transaction newTransaction = convertToDatabaseTransactionOrThrow(saveTransactionRequest);
        Long newId = transactionsRepository.save(newTransaction).getId();
        return new ResponseEntity<>(new CreatedResponse().createdId(newId), HttpStatus.CREATED);
    }

    @Override
    @Transactional
    public ResponseEntity<Void> deleteTransaction(Long transactionId) {
        transactionsRepository.delete(findTransactionOrThrow(transactionId));
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<GetTransactionResponse> getTransaction(Long transactionId) {
        Transaction transaction = findTransactionOrThrow(transactionId);
        return new ResponseEntity<>(convertToResponseTransaction(transaction), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<GetTransactionResponse>> getTransactions(@Valid Optional<LocalDate> after,
                                                                        @Valid Optional<LocalDate> before,
                                                                        @Valid Optional<BigDecimal> accountId) {
        User principal = principalProvider.getPrincipal();
        List<GetTransactionResponse> transactions = transactionsRepository.findByUser(principal).stream()
                .map(this::convertToResponseTransaction)
                .collect(Collectors.toList());

        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> updateTransaction(Long transactionId,
                                                  @Valid SaveTransactionRequest saveTransactionRequest) {
        findTransactionOrThrow(transactionId);
        Transaction transaction = convertToDatabaseTransactionOrThrow(saveTransactionRequest);
        transaction.setId(transactionId);
        transactionsRepository.save(transaction);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private Transaction convertToDatabaseTransactionOrThrow(SaveTransactionRequest saveTransactionRequest) {
        Transaction transaction = new Transaction();
        transaction.setUser(principalProvider.getPrincipal());
        transaction.setDate(saveTransactionRequest.getDate());
        transaction.setName(saveTransactionRequest.getName());
        transaction.setEntries(saveTransactionRequest.getEntries().stream()
                .map(entry -> convertToDatabaseEntryOrThrow(entry, transaction))
                .collect(Collectors.toList()));

        if (transaction.getEntries().size() < 2) {
            throw new ParameterException("transaction must have two or more entries");
        } else if (!isEachEntryAccountUnique(transaction)) {
            throw new ParameterException("each account must only be referenced in at most one entry");
        } else if (!isTotalZero(transaction)) {
            throw new ParameterException("transaction total must be zero");
        }

        return transaction;
    }

    private Entry convertToDatabaseEntryOrThrow(SaveTransactionRequestEntries entry, Transaction transaction) {
        boolean verified = Optional.ofNullable(entry.getVerified()).orElse(false);
        Account account = accountsRepository.findById(entry.getAccountId())
                .orElseThrow(() -> new ParameterException("account " + entry.getAccountId() + " not found"));

        if (!account.getUser().equals(principalProvider.getPrincipal())) {
            throw new ParameterException("account " + entry.getAccountId() + " not found");
        }

        return new Entry(transaction, account, entry.getAmount(), verified);
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

    private Transaction findTransactionOrThrow(Long transactionId) {
        Transaction transaction = transactionsRepository.findById(transactionId)
                .orElseThrow(() -> new NotFoundException("transaction " + transactionId + " not found"));

        if (!transaction.getUser().equals(principalProvider.getPrincipal())) {
            throw new NotFoundException("transaction " + transactionId + " not found");
        }

        return transaction;
    }

    private GetTransactionResponse convertToResponseTransaction(Transaction transaction) {
        return new GetTransactionResponse()
                .id(transaction.getId())
                .date(transaction.getDate())
                .name(transaction.getName())
                .entries(transaction.getEntries().stream()
                        .map(this::convertToResponseEntry)
                        .collect(Collectors.toList()));
    }

    private GetTransactionResponseEntries convertToResponseEntry(Entry entry) {
        return new GetTransactionResponseEntries()
                .accountId(entry.getId().getAccount().getId())
                .amount(entry.getAmount())
                .verified(entry.isVerified());
    }

}
