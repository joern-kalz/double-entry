package com.github.joern.kalz.doubleentry.controllers;

import com.github.joern.kalz.doubleentry.generated.api.TransactionsApi;
import com.github.joern.kalz.doubleentry.generated.model.*;
import com.github.joern.kalz.doubleentry.models.Entry;
import com.github.joern.kalz.doubleentry.models.Transaction;
import com.github.joern.kalz.doubleentry.services.transactions.CreateTransactionRequest;
import com.github.joern.kalz.doubleentry.services.transactions.RequestEntry;
import com.github.joern.kalz.doubleentry.services.transactions.TransactionsService;
import com.github.joern.kalz.doubleentry.services.transactions.UpdateTransactionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class TransactionsApiImpl implements TransactionsApi {

    @Autowired
    private TransactionsService transactionsService;

    @Override
    public ResponseEntity<CreatedResponse> createTransaction(@Valid SaveTransactionRequest saveTransactionRequest) {
        CreateTransactionRequest createTransactionRequest = convertToCreateTransactionRequest(saveTransactionRequest);
        Transaction transaction = transactionsService.create(createTransactionRequest);
        CreatedResponse createdResponse = new CreatedResponse().createdId(transaction.getId());
        return new ResponseEntity<>(createdResponse, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> deleteTransaction(Long transactionId) {
        transactionsService.delete(transactionId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<GetTransactionResponse> getTransaction(Long transactionId) {
        Transaction transaction = transactionsService.findById(transactionId);
        GetTransactionResponse getTransactionResponse = convertToResponse(transaction);
        return new ResponseEntity<>(getTransactionResponse, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<GetTransactionResponse>> getTransactions(@Valid Optional<LocalDate> after,
                                                                        @Valid Optional<LocalDate> before,
                                                                        @Valid Optional<BigDecimal> accountId) {
        List<GetTransactionResponse> responseBody = transactionsService.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> updateTransaction(Long transactionId,
                                                  @Valid SaveTransactionRequest saveTransactionRequest) {
        UpdateTransactionRequest updateTransactionRequest = convertToUpdateTransactionRequest(transactionId,
                saveTransactionRequest);
        transactionsService.update(updateTransactionRequest);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private CreateTransactionRequest convertToCreateTransactionRequest(SaveTransactionRequest saveTransactionRequest) {
        CreateTransactionRequest createTransactionRequest = new CreateTransactionRequest();
        createTransactionRequest.setDate(saveTransactionRequest.getDate());
        createTransactionRequest.setName(saveTransactionRequest.getName());
        createTransactionRequest.setEntries(convertToRequestEntries(saveTransactionRequest.getEntries()));
        return createTransactionRequest;
    }

    private UpdateTransactionRequest convertToUpdateTransactionRequest(long id,
                                                                       SaveTransactionRequest saveTransactionRequest) {
        UpdateTransactionRequest updateTransactionRequest = new UpdateTransactionRequest();
        updateTransactionRequest.setId(id);
        updateTransactionRequest.setDate(saveTransactionRequest.getDate());
        updateTransactionRequest.setName(saveTransactionRequest.getName());
        updateTransactionRequest.setEntries(convertToRequestEntries(saveTransactionRequest.getEntries()));
        return updateTransactionRequest;
    }

    private List<RequestEntry> convertToRequestEntries(List<SaveTransactionRequestEntries> entries) {
        return entries.stream()
                .map(entry -> {
                    RequestEntry requestEntry = new RequestEntry();
                    requestEntry.setAccountId(entry.getAccountId());
                    requestEntry.setAmount(entry.getAmount());
                    requestEntry.setVerified(Optional.ofNullable(entry.getVerified()).orElse(false));
                    return requestEntry;
                })
                .collect(Collectors.toList());
    }

    public GetTransactionResponse convertToResponse(Transaction transaction) {
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
