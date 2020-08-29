package com.github.joern.kalz.doubleentry.controllers;

import com.github.joern.kalz.doubleentry.generated.api.AccountsApi;
import com.github.joern.kalz.doubleentry.generated.model.ApiCreatedResponse;
import com.github.joern.kalz.doubleentry.generated.model.ApiAccount;
import com.github.joern.kalz.doubleentry.generated.model.ApiSaveAccountRequest;
import com.github.joern.kalz.doubleentry.models.Account;
import com.github.joern.kalz.doubleentry.services.accounts.AccountsService;
import com.github.joern.kalz.doubleentry.services.accounts.CreateAccountRequest;
import com.github.joern.kalz.doubleentry.services.accounts.UpdateAccountRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class AccountsApiImpl implements AccountsApi {

    @Autowired
    private AccountsService accountsService;

    @Autowired
    private ResponseFactory responseFactory;

    @Override
    public ResponseEntity<ApiCreatedResponse> createAccount(@Valid ApiSaveAccountRequest saveAccountRequest) {
        CreateAccountRequest createAccountRequest = createCreateAccountRequest(saveAccountRequest);
        Account account = accountsService.createAccount(createAccountRequest);
        ApiCreatedResponse createdResponse = new ApiCreatedResponse().createdId(account.getId());
        return new ResponseEntity<>(createdResponse, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<List<ApiAccount>> getAccounts() {
        List<ApiAccount> responseBody = accountsService.findAll().stream()
                .map(responseFactory::convertToResponse)
                .collect(Collectors.toList());

        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> updateAccount(Long accountId, @Valid ApiSaveAccountRequest saveAccountRequest) {
        UpdateAccountRequest updateAccountRequest = createUpdateAccountRequest(accountId, saveAccountRequest);
        accountsService.updateAccount(updateAccountRequest);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private CreateAccountRequest createCreateAccountRequest(ApiSaveAccountRequest saveAccountRequest) {
        CreateAccountRequest createAccountRequest = new CreateAccountRequest();
        createAccountRequest.setName(saveAccountRequest.getName());
        createAccountRequest.setParentId(saveAccountRequest.getParentId());
        createAccountRequest.setActive(saveAccountRequest.getActive());
        return createAccountRequest;
    }

    private UpdateAccountRequest createUpdateAccountRequest(long id, ApiSaveAccountRequest saveAccountRequest) {
        UpdateAccountRequest updateAccountRequest = new UpdateAccountRequest();
        updateAccountRequest.setId(id);
        updateAccountRequest.setName(saveAccountRequest.getName());
        updateAccountRequest.setParentId(saveAccountRequest.getParentId());
        updateAccountRequest.setActive(saveAccountRequest.getActive());
        return updateAccountRequest;
    }
}
