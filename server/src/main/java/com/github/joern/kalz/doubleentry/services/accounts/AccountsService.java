package com.github.joern.kalz.doubleentry.services.accounts;

import com.github.joern.kalz.doubleentry.services.exceptions.NotFoundException;
import com.github.joern.kalz.doubleentry.services.exceptions.ParameterException;
import com.github.joern.kalz.doubleentry.models.Account;
import com.github.joern.kalz.doubleentry.models.AccountsRepository;
import com.github.joern.kalz.doubleentry.services.PrincipalProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AccountsService {

    @Autowired
    private AccountsRepository accountsRepository;

    @Autowired
    private PrincipalProvider principalProvider;

    @Autowired
    private AccountsConverter accountsConverter;

    @Autowired
    private AccountsValidator accountsValidator;

    public List<Account> findAll() {
        return accountsRepository.findByUser(principalProvider.getPrincipal());
    }

    public Account findById(long id) {
        return findAccount(id)
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

        findAccount(account.getId())
                .orElseThrow(() -> new NotFoundException("account " + account.getId() + " not found"));

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
                throw new RuntimeException("unknown validation error " + validationResult);
        }
    }

    private Optional<Account> findAccount(long id) {
        Optional<Account> account = accountsRepository.findById(id);

        if (account.isEmpty() || !principalProvider.getPrincipal().equals(account.get().getUser())) {
            return Optional.empty();
        }

        return account;
    }
}
