package com.github.joern.kalz.doubleentry.controllers;

import com.github.joern.kalz.doubleentry.generated.model.GetTransactionResponse;
import com.github.joern.kalz.doubleentry.generated.model.GetTransactionResponseEntries;
import com.github.joern.kalz.doubleentry.models.Entry;
import com.github.joern.kalz.doubleentry.models.Transaction;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class TransactionResponseFactory {
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
