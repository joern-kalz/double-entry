package com.github.joern.kalz.doubleentry.services.accounts;

import com.github.joern.kalz.doubleentry.models.Account;
import com.github.joern.kalz.doubleentry.services.AccountProvider;
import com.github.joern.kalz.doubleentry.services.PrincipalProvider;
import com.github.joern.kalz.doubleentry.services.exceptions.ParameterException;
import org.springframework.stereotype.Service;

@Service
public class AccountsConverter {

    private final PrincipalProvider principalProvider;
    private final AccountProvider accountProvider;

    public AccountsConverter(PrincipalProvider principalProvider, AccountProvider accountProvider) {
        this.principalProvider = principalProvider;
        this.accountProvider = accountProvider;
    }

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
        return id == null ? null : accountProvider.find(id)
                .orElseThrow(() -> new ParameterException("parent " + id + " not found"));
    }

}
