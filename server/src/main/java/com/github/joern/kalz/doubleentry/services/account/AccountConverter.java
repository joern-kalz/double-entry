package com.github.joern.kalz.doubleentry.services.account;

import com.github.joern.kalz.doubleentry.controllers.exceptions.ParameterException;
import com.github.joern.kalz.doubleentry.model.Account;
import com.github.joern.kalz.doubleentry.model.AccountsRepository;
import com.github.joern.kalz.doubleentry.services.PrincipalProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountConverter {

    @Autowired
    private AccountsRepository accountsRepository;

    @Autowired
    private PrincipalProvider principalProvider;

    public Account convertToAccount(CreateAccountRequest createAccountRequest) {
        Account account = new Account();
        account.setUser(principalProvider.getPrincipal());
        account.setName(createAccountRequest.getName());
        account.setParent(findParent(createAccountRequest.getParentId()));
        account.setActive(createAccountRequest.isActive());
        return account;
    }

    public Account convertToAccount(UpdateAccountRequest updateAccountRequest) {
        Account account = new Account();
        account.setUser(principalProvider.getPrincipal());
        account.setId(updateAccountRequest.getId());
        account.setName(updateAccountRequest.getName());
        account.setParent(findParent(updateAccountRequest.getParentId()));
        account.setActive(updateAccountRequest.isActive());
        return account;
    }

    private Account findParent(Long id) {
        return accountsRepository.findById(id)
                .orElseThrow(() -> new ParameterException("parent " + id + " not found"));
    }

}
