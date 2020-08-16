package com.github.joern.kalz.doubleentry.controllers;

import com.github.joern.kalz.doubleentry.controllers.exceptions.NotFoundException;
import com.github.joern.kalz.doubleentry.controllers.exceptions.ParameterException;
import com.github.joern.kalz.doubleentry.generated.api.AccountsApi;
import com.github.joern.kalz.doubleentry.generated.model.CreatedResponse;
import com.github.joern.kalz.doubleentry.generated.model.GetAccountResponse;
import com.github.joern.kalz.doubleentry.generated.model.SaveAccountRequest;
import com.github.joern.kalz.doubleentry.model.Account;
import com.github.joern.kalz.doubleentry.model.AccountsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
public class AccountsApiImpl implements AccountsApi {

    public static final int MAXIMUM_ACCOUNT_HIERARCHY_DEPTH = 100;
    @Autowired
    private AccountsRepository accountsRepository;

    @Override
    public ResponseEntity<CreatedResponse> createAccount(@Valid SaveAccountRequest saveAccountRequest) {
        if (saveAccountRequest.getName() == null) {
            throw new ParameterException("name missing");
        }

        if (saveAccountRequest.getParentId() == null) {
            throw new ParameterException("parentId missing");
        }

        String name = getValidAccountNameOrThrow(saveAccountRequest);
        Account parent = findAccountByIdOrThrow(saveAccountRequest.getParentId());
        boolean active = saveAccountRequest.getActive() != null ? saveAccountRequest.getActive() : true;

        accountsRepository.save(new Account(parent, name, active));

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<List<GetAccountResponse>> getAccounts() {
        Spliterator<Account> accounts = accountsRepository.findAll().spliterator();

        List<GetAccountResponse> responseBody = StreamSupport.stream(accounts, false)
                .map(account -> new GetAccountResponse()
                    .id(account.getId())
                    .name(account.getName())
                    .parentId(account.getParent() != null ? account.getParent().getId() : null)
                    .active(account.isActive()))
                .collect(Collectors.toList());

        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> updateAccount(Long accountId, @Valid SaveAccountRequest saveAccountRequest) {
        Account account = accountsRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("account " + accountId));

        if (saveAccountRequest.getName() != null) {
            account.setName(getValidAccountNameOrThrow(saveAccountRequest));
        }

        if (saveAccountRequest.getParentId() != null) {
            account.setParent(getValidParentForAccountOrThrow(saveAccountRequest, account));
        }

        if (saveAccountRequest.getActive() != null) {
            account.setActive(saveAccountRequest.getActive());
        }

        accountsRepository.save(account);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private Account findAccountByIdOrThrow(Long accountId) {
        return accountsRepository
                .findById(accountId)
                .orElseThrow(() -> new ParameterException("account " + accountId));
    }

    private String getValidAccountNameOrThrow(SaveAccountRequest saveAccountRequest) {
        if (saveAccountRequest.getName().isBlank()) {
            throw new ParameterException("name invalid");
        }

        return saveAccountRequest.getName();
    }

    private Account getValidParentForAccountOrThrow(SaveAccountRequest saveAccountRequest, Account account) {
        Account newParent = findAccountByIdOrThrow(saveAccountRequest.getParentId());

        Account examinedAccount = newParent;

        for (int i = 0; i < MAXIMUM_ACCOUNT_HIERARCHY_DEPTH && examinedAccount != null; i++) {
            if (examinedAccount.getId().equals(account.getId())) {
                throw new ParameterException("cyclic parent child relationship between " + account.getId() + " and " +
                        newParent.getId());
            }

            examinedAccount = examinedAccount.getParent();
        }

        if (examinedAccount != null) {
            throw new ParameterException("maximum hierarchy depth exceeded");
        }

        return newParent;
    }
}
