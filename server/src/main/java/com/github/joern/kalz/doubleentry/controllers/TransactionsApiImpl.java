package com.github.joern.kalz.doubleentry.controllers;

import com.github.joern.kalz.doubleentry.generated.api.TransactionsApi;
import com.github.joern.kalz.doubleentry.generated.model.ApiCreatedResponse;
import com.github.joern.kalz.doubleentry.generated.model.ApiTransaction;
import com.github.joern.kalz.doubleentry.generated.model.ApiSaveTransactionRequest;
import com.github.joern.kalz.doubleentry.generated.model.ApiSaveTransactionRequestEntries;
import com.github.joern.kalz.doubleentry.models.Transaction;
import com.github.joern.kalz.doubleentry.models.TransactionOrder;
import com.github.joern.kalz.doubleentry.services.transactions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class TransactionsApiImpl implements TransactionsApi {

    @Autowired
    private TransactionsService transactionsService;

    @Autowired
    private ResponseFactory responseFactory;

    @Override
    public ResponseEntity<ApiCreatedResponse> createTransaction(@Valid ApiSaveTransactionRequest saveTransactionRequest) {
        CreateTransactionRequest createTransactionRequest = convertToCreateTransactionRequest(saveTransactionRequest);
        Transaction transaction = transactionsService.create(createTransactionRequest);
        ApiCreatedResponse createdResponse = new ApiCreatedResponse().createdId(transaction.getId());
        return new ResponseEntity<>(createdResponse, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> deleteTransaction(Long transactionId) {
        transactionsService.delete(transactionId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<ApiTransaction> getTransaction(Long transactionId) {
        Transaction transaction = transactionsService.findById(transactionId);
        ApiTransaction getTransactionResponse = responseFactory.convertToResponse(transaction);
        return new ResponseEntity<>(getTransactionResponse, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<ApiTransaction>> getTransactions(@Valid LocalDate after, @Valid LocalDate before,
        @Valid Long accountId, @Valid Long debitAccountId, @Valid Long creditAccountId, @Valid String name,
        @Valid Integer page, @Valid Integer size, @Valid String sort) {

        Integer pageOffset = page != null ?
                page * Optional.ofNullable(size).orElse(1) :
                null;

        TransactionOrder order = "dateDescending".equals(sort) ?
                TransactionOrder.DATE_DESCENDING :
                TransactionOrder.DATE_ASCENDING;

        FindTransactionsRequest request = new FindTransactionsRequest();
        request.setAfter(after);
        request.setBefore(before);
        request.setAccountId(accountId);
        request.setCreditAccountId(creditAccountId);
        request.setDebitAccountId(debitAccountId);
        request.setName(name);
        request.setPageOffset(pageOffset);
        request.setMaxPageSize(size);
        request.setOrder(order);

        List<ApiTransaction> responseBody = transactionsService
                .find(request)
                .stream()
                .map(responseFactory::convertToResponse)
                .collect(Collectors.toList());

        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> updateTransaction(Long transactionId,
                                                  @Valid ApiSaveTransactionRequest saveTransactionRequest) {
        UpdateTransactionRequest updateTransactionRequest = convertToUpdateTransactionRequest(transactionId,
                saveTransactionRequest);
        transactionsService.update(updateTransactionRequest);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private CreateTransactionRequest convertToCreateTransactionRequest(ApiSaveTransactionRequest saveTransactionRequest) {
        CreateTransactionRequest createTransactionRequest = new CreateTransactionRequest();
        createTransactionRequest.setDate(saveTransactionRequest.getDate());
        createTransactionRequest.setName(saveTransactionRequest.getName());
        createTransactionRequest.setEntries(convertToRequestEntries(saveTransactionRequest.getEntries()));
        return createTransactionRequest;
    }

    private UpdateTransactionRequest convertToUpdateTransactionRequest(long id,
                                                                       ApiSaveTransactionRequest saveTransactionRequest) {
        UpdateTransactionRequest updateTransactionRequest = new UpdateTransactionRequest();
        updateTransactionRequest.setId(id);
        updateTransactionRequest.setDate(saveTransactionRequest.getDate());
        updateTransactionRequest.setName(saveTransactionRequest.getName());
        updateTransactionRequest.setEntries(convertToRequestEntries(saveTransactionRequest.getEntries()));
        return updateTransactionRequest;
    }

    private List<RequestEntry> convertToRequestEntries(List<ApiSaveTransactionRequestEntries> entries) {
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
}
