package com.github.joern.kalz.doubleentry.controllers;

import com.github.joern.kalz.doubleentry.controllers.exceptions.NotFoundException;
import com.github.joern.kalz.doubleentry.controllers.exceptions.ParameterException;
import com.github.joern.kalz.doubleentry.generated.api.AccountsApi;
import com.github.joern.kalz.doubleentry.generated.model.CreatedResponse;
import com.github.joern.kalz.doubleentry.generated.model.GetAccountResponse;
import com.github.joern.kalz.doubleentry.generated.model.SaveAccountRequest;
import com.github.joern.kalz.doubleentry.model.Account;
import com.github.joern.kalz.doubleentry.model.AccountsRepository;
import com.github.joern.kalz.doubleentry.model.User;
import com.github.joern.kalz.doubleentry.services.PrincipalProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
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

    @Autowired
    private PrincipalProvider principalProvider;

    @Override
    @Transactional
    public ResponseEntity<CreatedResponse> createAccount(@Valid SaveAccountRequest saveAccountRequest) {
        Account newAccount = convertToAccount(null, saveAccountRequest);
        long newId = accountsRepository.save(newAccount).getId();
        return new ResponseEntity<>(new CreatedResponse().createdId(newId), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<List<GetAccountResponse>> getAccounts() {
        User principal = principalProvider.getPrincipal();
        Spliterator<Account> accounts = accountsRepository.findByUser(principal).spliterator();

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
    @Transactional
    public ResponseEntity<Void> updateAccount(Long accountId, @Valid SaveAccountRequest saveAccountRequest) {
        Account account = accountsRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("account " + accountId));

        if (!account.getUser().equals(principalProvider.getPrincipal())) {
            throw new NotFoundException("account " + accountId);
        }

        Account updatedAccount = convertToAccount(accountId, saveAccountRequest);
        accountsRepository.save(updatedAccount);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private Account convertToAccount(Long accountId, SaveAccountRequest saveAccountRequest) {
        Long parentId = saveAccountRequest.getParentId();
        Account parent = accountsRepository.findById(parentId)
                .orElseThrow(() -> new ParameterException("parent " + parentId + " not found"));

        Account account = new Account();
        account.setId(accountId);
        account.setUser(principalProvider.getPrincipal());
        account.setName(saveAccountRequest.getName());
        account.setParent(parent);
        account.setActive(saveAccountRequest.getActive());

        validateParentChildRelationship(parent, account);

        return account;
    }

    private void validateParentChildRelationship(Account parent, Account account) {
        Account currentPosition = parent;

        for (int i = 0; i < MAXIMUM_ACCOUNT_HIERARCHY_DEPTH && currentPosition != null; i++) {
            if (currentPosition.getId().equals(account.getId())) {
                throw new ParameterException("cyclic parent child relationship between " + account.getId() + " and " +
                        parent.getId());
            }

            currentPosition = currentPosition.getParent();
        }

        if (currentPosition != null) {
            throw new ParameterException("maximum hierarchy depth exceeded");
        }
    }
}
