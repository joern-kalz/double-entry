package com.github.joern.kalz.doubleentry.controllers;

import com.github.joern.kalz.doubleentry.generated.model.ApiAccount;
import com.github.joern.kalz.doubleentry.generated.model.ApiTransaction;
import com.github.joern.kalz.doubleentry.generated.model.ApiTransactionEntries;
import com.github.joern.kalz.doubleentry.models.Account;
import com.github.joern.kalz.doubleentry.models.Entry;
import com.github.joern.kalz.doubleentry.models.Transaction;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class ResponseFactory {
    public ApiTransaction convertToResponse(Transaction transaction) {
        return new ApiTransaction()
                .id(transaction.getId())
                .date(transaction.getDate())
                .name(transaction.getName())
                .entries(transaction.getEntries().stream()
                        .map(this::convertToResponse)
                        .collect(Collectors.toList()));
    }

    public ApiTransactionEntries convertToResponse(Entry entry) {
        return new ApiTransactionEntries()
                .accountId(entry.getId().getAccount().getId())
                .amount(entry.getAmount().toString())
                .verified(entry.isVerified());
    }

    public ApiAccount convertToResponse(Account account) {
        return new ApiAccount()
                .id(account.getId())
                .name(account.getName())
                .parentId(account.getParent() != null ? account.getParent().getId() : null)
                .active(account.isActive());
    }
}
