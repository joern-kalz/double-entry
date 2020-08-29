package com.github.joern.kalz.doubleentry.controllers;

import com.github.joern.kalz.doubleentry.generated.model.*;
import com.github.joern.kalz.doubleentry.services.repository.ImportRepositoryRequest;
import com.github.joern.kalz.doubleentry.services.repository.ImportRepositoryRequestAccount;
import com.github.joern.kalz.doubleentry.services.repository.ImportRepositoryRequestEntry;
import com.github.joern.kalz.doubleentry.services.repository.ImportRepositoryRequestTransaction;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class RequestFactory {
    public ImportRepositoryRequest convertToRequest(ApiRepository repository) {
        ImportRepositoryRequest request = new ImportRepositoryRequest();

        request.setAccounts(repository.getAccounts().stream()
                .map(this::convertAccount)
                .collect(Collectors.toList()));

        request.setTransactions(repository.getTransactions().stream()
                .map(this::convertTransaction)
                .collect(Collectors.toList()));

        return request;
    }

    private ImportRepositoryRequestAccount convertAccount(ApiAccount account) {
        ImportRepositoryRequestAccount importAccount = new ImportRepositoryRequestAccount();
        importAccount.setId(account.getId());
        importAccount.setName(account.getName());
        importAccount.setParentId(account.getParentId());
        importAccount.setActive(account.getActive());
        return importAccount;
    }

    private ImportRepositoryRequestTransaction convertTransaction(ApiTransaction transaction) {
        ImportRepositoryRequestTransaction importTransaction = new ImportRepositoryRequestTransaction();
        importTransaction.setId(transaction.getId());
        importTransaction.setDate(transaction.getDate());
        importTransaction.setName(transaction.getName());
        importTransaction.setEntries(transaction.getEntries().stream()
                .map(this::convertEntry)
                .collect(Collectors.toList()));

        return importTransaction;
    }

    private ImportRepositoryRequestEntry convertEntry(ApiTransactionEntries entry) {
        ImportRepositoryRequestEntry importEntry = new ImportRepositoryRequestEntry();
        importEntry.setAccountId(entry.getAccountId());
        importEntry.setAmount(entry.getAmount());
        importEntry.setVerified(entry.getVerified());
        return importEntry;
    }
}
