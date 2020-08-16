package com.github.joern.kalz.doubleentry.controllers;

import com.github.joern.kalz.doubleentry.generated.api.TransactionsApi;
import com.github.joern.kalz.doubleentry.generated.model.CreatedResponse;
import com.github.joern.kalz.doubleentry.generated.model.GetTransactionResponse;
import com.github.joern.kalz.doubleentry.generated.model.SaveTransactionRequest;
import org.springframework.http.ResponseEntity;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class TransactionsApiImpl implements TransactionsApi {
    @Override
    public ResponseEntity<CreatedResponse> createTransaction(@Valid SaveTransactionRequest saveTransactionRequest) {
        return null;
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
}
