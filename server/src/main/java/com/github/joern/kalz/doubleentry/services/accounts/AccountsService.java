package com.github.joern.kalz.doubleentry.services.accounts;

import com.github.joern.kalz.doubleentry.models.Account;
import com.github.joern.kalz.doubleentry.models.AccountsRepository;
import com.github.joern.kalz.doubleentry.services.AccountProvider;
import com.github.joern.kalz.doubleentry.services.PrincipalProvider;
import com.github.joern.kalz.doubleentry.services.exceptions.NotFoundException;
import com.github.joern.kalz.doubleentry.services.exceptions.ParameterException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AccountsService {

    private final AccountsRepository accountsRepository;
    private final PrincipalProvider principalProvider;
    private final AccountsConverter accountsConverter;
    private final AccountsValidator accountsValidator;
    private final AccountProvider accountProvider;

    public AccountsService(AccountsRepository accountsRepository, PrincipalProvider principalProvider,
                           AccountsConverter accountsConverter, AccountsValidator accountsValidator,
                           AccountProvider accountProvider) {
        this.accountsRepository = accountsRepository;
        this.principalProvider = principalProvider;
        this.accountsConverter = accountsConverter;
        this.accountsValidator = accountsValidator;
        this.accountProvider = accountProvider;
    }

    public List<Account> findAll() {
        return accountsRepository.findByUser(principalProvider.getPrincipal());
    }

    public Account findById(long id) {
        return accountProvider.find(id)
                .orElseThrow(() -> new NotFoundException("account " + id + " not found"));
    }

    @Transactional
    public Account createAccount(CreateAccountRequest createAccountRequest) {
        Account account = accountsConverter.convertToAccount(createAccountRequest);
        AccountsValidator.Result validationResult = accountsValidator.validate(account);

        if (validationResult != AccountsValidator.Result.OK) {
            throw createException(validationResult, account);
        }

        return accountsRepository.save(account);
    }

    @Transactional
    public void updateAccount(UpdateAccountRequest updateAccountRequest) {
        Account account = accountsConverter.convertToAccount(updateAccountRequest);
        AccountsValidator.Result validationResult = accountsValidator.validate(account);

        if (validationResult != AccountsValidator.Result.OK) {
            throw createException(validationResult, account);
        }

        if (accountProvider.find(account.getId()).isEmpty()) {
            throw  new NotFoundException("account " + account.getId() + " not found");
        }

        accountsRepository.save(account);
    }

    private RuntimeException createException(AccountsValidator.Result validationResult, Account account) {
        switch (validationResult) {
            case CYCLIC_PARENT_CHILD_RELATIONSHIP:
                return new ParameterException("cyclic parent child relationship between " + account.getId() +
                        " and " + account.getParent().getId());
            case MAXIMUM_HIERARCHY_DEPTH_EXCEEDED:
                return new ParameterException("maximum hierarchy depth exceeded");
            default:
                throw new IllegalArgumentException("unknown validation error " + validationResult);
        }
    }
}
