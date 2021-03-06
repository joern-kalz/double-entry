package com.github.joern.kalz.doubleentry.services;

import com.github.joern.kalz.doubleentry.models.Account;
import com.github.joern.kalz.doubleentry.models.AccountsRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AccountProvider {

    private final AccountsRepository accountsRepository;
    private final PrincipalProvider principalProvider;

    public AccountProvider(AccountsRepository accountsRepository, PrincipalProvider principalProvider) {
        this.accountsRepository = accountsRepository;
        this.principalProvider = principalProvider;
    }

    public Optional<Account> find(long id) {
        Optional<Account> account = accountsRepository.findById(id);

        if (account.isEmpty() || !principalProvider.getPrincipal().equals(account.get().getUser())) {
            return Optional.empty();
        }

        return account;
    }
}
