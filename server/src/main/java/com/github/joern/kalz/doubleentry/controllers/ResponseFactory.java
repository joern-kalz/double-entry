package com.github.joern.kalz.doubleentry.controllers;

import com.github.joern.kalz.doubleentry.generated.model.GetAccountResponse;
import com.github.joern.kalz.doubleentry.generated.model.GetTransactionResponse;
import com.github.joern.kalz.doubleentry.generated.model.GetTransactionResponseEntries;
import com.github.joern.kalz.doubleentry.models.Account;
import com.github.joern.kalz.doubleentry.models.Entry;
import com.github.joern.kalz.doubleentry.models.Transaction;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class ResponseFactory {
    public GetTransactionResponse convertToResponse(Transaction transaction) {
        return new GetTransactionResponse()
                .id(transaction.getId())
                .date(transaction.getDate())
                .name(transaction.getName())
                .entries(transaction.getEntries().stream()
                        .map(this::convertToResponse)
                        .collect(Collectors.toList()));
    }

    public GetTransactionResponseEntries convertToResponse(Entry entry) {
        return new GetTransactionResponseEntries()
                .accountId(entry.getId().getAccount().getId())
                .amount(entry.getAmount())
                .verified(entry.isVerified());
    }

    public GetAccountResponse convertToResponse(Account account) {
        return new GetAccountResponse()
                .id(account.getId())
                .name(account.getName())
                .parentId(account.getParent() != null ? account.getParent().getId() : null)
                .active(account.isActive());
    }
}
