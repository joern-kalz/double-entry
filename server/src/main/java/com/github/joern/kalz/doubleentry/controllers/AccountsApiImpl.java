package com.github.joern.kalz.doubleentry.controllers;

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

@RestController
public class AccountsApiImpl implements AccountsApi {

    @Autowired
    private AccountsRepository accountsRepository;

    @Override
    public ResponseEntity<CreatedResponse> createAccount(@Valid SaveAccountRequest saveAccountRequest) {
        String name = saveAccountRequest.getName();

        if (name == null || name.isBlank()) {
            throw new ParameterException("name missing");
        }

        Integer parentId = saveAccountRequest.getParentId();

        if (parentId == null) {
            throw new ParameterException("parentId missing");
        }

        Account parent = accountsRepository
                .findById(parentId.longValue())
                .orElseThrow(() -> new ParameterException("parent account " + parentId + " not found"));

        boolean active = saveAccountRequest.getActive() != null ? saveAccountRequest.getActive() : true;

        accountsRepository.save(new Account(parent, name, active));

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<List<GetAccountResponse>> getAccounts() {
        return null;
    }

    @Override
    public ResponseEntity<Void> updateAccount(Integer accountId, @Valid SaveAccountRequest saveAccountRequest) {
        return null;
    }

}
