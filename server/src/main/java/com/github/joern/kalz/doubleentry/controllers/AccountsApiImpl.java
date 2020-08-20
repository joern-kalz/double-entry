package com.github.joern.kalz.doubleentry.controllers;

import com.github.joern.kalz.doubleentry.generated.api.AccountsApi;
import com.github.joern.kalz.doubleentry.generated.model.CreatedResponse;
import com.github.joern.kalz.doubleentry.generated.model.GetAccountResponse;
import com.github.joern.kalz.doubleentry.generated.model.SaveAccountRequest;
import com.github.joern.kalz.doubleentry.model.Account;
import com.github.joern.kalz.doubleentry.services.account.AccountService;
import com.github.joern.kalz.doubleentry.services.account.CreateAccountRequest;
import com.github.joern.kalz.doubleentry.services.account.UpdateAccountRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class AccountsApiImpl implements AccountsApi {

    @Autowired
    private AccountService accountService;

    @Override
    public ResponseEntity<CreatedResponse> createAccount(@Valid SaveAccountRequest saveAccountRequest) {
        CreateAccountRequest createAccountRequest = createCreateAccountRequest(saveAccountRequest);
        Account account = accountService.createAccount(createAccountRequest);
        CreatedResponse createdResponse = new CreatedResponse().createdId(account.getId());
        return new ResponseEntity<>(createdResponse, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<List<GetAccountResponse>> getAccounts() {
        List<GetAccountResponse> responseBody = accountService.findAll().stream()
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
        UpdateAccountRequest updateAccountRequest = createUpdateAccountRequest(accountId, saveAccountRequest);
        accountService.updateAccount(updateAccountRequest);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private CreateAccountRequest createCreateAccountRequest(SaveAccountRequest saveAccountRequest) {
        CreateAccountRequest createAccountRequest = new CreateAccountRequest();
        createAccountRequest.setName(saveAccountRequest.getName());
        createAccountRequest.setParentId(saveAccountRequest.getParentId());
        createAccountRequest.setActive(saveAccountRequest.getActive());
        return createAccountRequest;
    }

    private UpdateAccountRequest createUpdateAccountRequest(long id, SaveAccountRequest saveAccountRequest) {
        UpdateAccountRequest updateAccountRequest = new UpdateAccountRequest();
        updateAccountRequest.setId(id);
        updateAccountRequest.setName(saveAccountRequest.getName());
        updateAccountRequest.setParentId(saveAccountRequest.getParentId());
        updateAccountRequest.setActive(saveAccountRequest.getActive());
        return updateAccountRequest;
    }
}
