package com.github.joern.kalz.doubleentry.services.transactions;

import com.github.joern.kalz.doubleentry.models.Account;
import com.github.joern.kalz.doubleentry.models.Entry;
import com.github.joern.kalz.doubleentry.models.EntryId;
import com.github.joern.kalz.doubleentry.models.Transaction;
import com.github.joern.kalz.doubleentry.services.AccountProvider;
import com.github.joern.kalz.doubleentry.services.PrincipalProvider;
import com.github.joern.kalz.doubleentry.services.exceptions.ParameterException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class TransactionsConverter {

    private final PrincipalProvider principalProvider;
    private final AccountProvider accountProvider;

    public TransactionsConverter(PrincipalProvider principalProvider, AccountProvider accountProvider) {
        this.principalProvider = principalProvider;
        this.accountProvider = accountProvider;
    }

    public Transaction convertToTransaction(CreateTransactionRequest createRequest) {
        Transaction transaction = new Transaction();
        transaction.setUser(principalProvider.getPrincipal());
        transaction.setDate(createRequest.getDate());
        transaction.setName(createRequest.getName());
        transaction.setEntries(createRequest.getEntries().stream()
                .map(entry -> convertToTransactionEntry(entry, transaction))
                .collect(Collectors.toList()));

        return transaction;
    }

    public Transaction convertToTransaction(UpdateTransactionRequest updateRequest) {
        Transaction transaction = new Transaction();
        transaction.setId(updateRequest.getId());
        transaction.setUser(principalProvider.getPrincipal());
        transaction.setDate(updateRequest.getDate());
        transaction.setName(updateRequest.getName());
        transaction.setEntries(updateRequest.getEntries().stream()
                .map(entry -> convertToTransactionEntry(entry, transaction))
                .collect(Collectors.toList()));

        return transaction;
    }

    private Entry convertToTransactionEntry(RequestEntry requestEntry, Transaction transaction) {
        boolean verified = requestEntry.isVerified();
        Account account = accountProvider.find(requestEntry.getAccountId())
                .orElseThrow(() -> new ParameterException("account " + requestEntry.getAccountId() + " not found"));

        if (!account.getUser().equals(principalProvider.getPrincipal())) {
            throw new ParameterException("account " + requestEntry.getAccountId() + " not found");
        }

        EntryId entryId = new EntryId();
        entryId.setAccount(account);
        entryId.setTransaction(transaction);

        Entry entry = new Entry();
        entry.setId(entryId);
        entry.setAmount(requestEntry.getAmount());
        entry.setVerified(verified);

        return entry;
    }
}
