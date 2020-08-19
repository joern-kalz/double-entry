package com.github.joern.kalz.doubleentry.services.transaction;

import com.github.joern.kalz.doubleentry.controllers.exceptions.ParameterException;
import com.github.joern.kalz.doubleentry.model.Account;
import com.github.joern.kalz.doubleentry.model.AccountsRepository;
import com.github.joern.kalz.doubleentry.model.Entry;
import com.github.joern.kalz.doubleentry.model.Transaction;
import com.github.joern.kalz.doubleentry.services.PrincipalProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class TransactionConverter {

    @Autowired
    private PrincipalProvider principalProvider;

    @Autowired
    private AccountsRepository accountsRepository;

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

    private Entry convertToTransactionEntry(RequestEntry entry, Transaction transaction) {
        boolean verified = entry.getVerified();
        Account account = accountsRepository.findById(entry.getAccountId())
                .orElseThrow(() -> new ParameterException("account " + entry.getAccountId() + " not found"));

        if (!account.getUser().equals(principalProvider.getPrincipal())) {
            throw new ParameterException("account " + entry.getAccountId() + " not found");
        }

        return new Entry(transaction, account, entry.getAmount(), verified);
    }
}
