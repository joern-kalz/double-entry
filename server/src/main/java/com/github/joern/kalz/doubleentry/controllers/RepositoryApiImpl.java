package com.github.joern.kalz.doubleentry.controllers;

import com.github.joern.kalz.doubleentry.generated.api.RepositoryApi;
import com.github.joern.kalz.doubleentry.generated.model.GetAccountResponse;
import com.github.joern.kalz.doubleentry.generated.model.GetTransactionResponse;
import com.github.joern.kalz.doubleentry.generated.model.Repository;
import com.github.joern.kalz.doubleentry.services.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class RepositoryApiImpl implements RepositoryApi {
    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private ResponseFactory responseFactory;

    @Override
    public ResponseEntity<Repository> exportRepository() {
        GetRepositoryResponse getRepositoryResponse = repositoryService.getRepository();

        Repository responseBody = new Repository();
        responseBody.setAccounts(getRepositoryResponse.getAccounts().stream()
                .map(responseFactory::convertToResponse)
                .collect(Collectors.toList()));
        responseBody.setTransactions(getRepositoryResponse.getTransactions().stream()
                .map(responseFactory::convertToResponse)
                .collect(Collectors.toList()));

        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> importRepository(@Valid Repository repository) {
        ImportRepositoryRequest request = new ImportRepositoryRequest();

        request.setAccounts(repository.getAccounts().stream()
                .map(this::convertAccount)
                .collect(Collectors.toList()));

        request.setTransactions(repository.getTransactions().stream()
                .map(this::convertTransaction)
                .collect(Collectors.toList()));

        repositoryService.importRepository(request);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private ImportRepositoryRequestAccount convertAccount(GetAccountResponse account) {
        ImportRepositoryRequestAccount importAccount = new ImportRepositoryRequestAccount();
        importAccount.setId(account.getId());
        importAccount.setName(account.getName());
        importAccount.setParentId(account.getParentId());
        importAccount.setActive(account.getActive());
        return importAccount;
    }

    private ImportRepositoryRequestTransaction convertTransaction(GetTransactionResponse transaction) {
        ImportRepositoryRequestTransaction importTransaction = new ImportRepositoryRequestTransaction();
        importTransaction.setId(transaction.getId());
        importTransaction.setDate(transaction.getDate());
        importTransaction.setName(transaction.getName());
        importTransaction.setEntries(transaction.getEntries().stream()
                .map(this::convertEntry)
                .collect(Collectors.toList()));

        return importTransaction;
    }

    private ImportRepositoryRequestEntry convertEntry(com.github.joern.kalz.doubleentry.generated.model.GetTransactionResponseEntries entry) {
        ImportRepositoryRequestEntry importEntry = new ImportRepositoryRequestEntry();
        importEntry.setAccountId(entry.getAccountId());
        importEntry.setAmount(entry.getAmount());
        importEntry.setVerified(entry.getVerified());
        return importEntry;
    }
}
