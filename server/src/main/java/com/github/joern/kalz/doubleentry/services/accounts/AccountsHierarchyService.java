package com.github.joern.kalz.doubleentry.services.accounts;

import com.github.joern.kalz.doubleentry.models.Account;
import com.github.joern.kalz.doubleentry.models.AccountsRepository;
import com.github.joern.kalz.doubleentry.services.PrincipalProvider;
import com.github.joern.kalz.doubleentry.services.exceptions.ParameterException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AccountsHierarchyService {

    private final AccountsRepository accountsRepository;
    private final PrincipalProvider principalProvider;

    public AccountsHierarchyService(AccountsRepository accountsRepository, PrincipalProvider principalProvider) {
        this.accountsRepository = accountsRepository;
        this.principalProvider = principalProvider;
    }

    public Map<Long, Account> getChildrenById(Long accountId) {
        List<Account> accounts = accountsRepository.findByUser(principalProvider.getPrincipal());

        Map<Long, Account> accountsById = accounts.stream()
                .collect(Collectors.toMap(Account::getId, Function.identity()));
        Map<Long, List<Account>> accountsByParentId = accounts.stream()
                .filter(account -> account.getParent() != null)
                .collect(Collectors.groupingBy(account -> account.getParent().getId()));
        Map<Long, Account> childrenById = new HashMap<>();

        if (!accountsById.containsKey(accountId)) {
            throw new ParameterException("account " + accountId + " not found");
        }

        Deque<Account> stack = new ArrayDeque<>(Collections.singletonList(accountsById.get(accountId)));

        while (!stack.isEmpty()) {
            Account account = stack.removeFirst();

            if (!accountsByParentId.containsKey(account.getId())) {
                continue;
            }

            for (Account child : accountsByParentId.get(account.getId())) {
                if (!childrenById.containsKey(child.getId())) {
                    childrenById.put(child.getId(), child);
                    stack.addLast(child);
                }
            }
        }

        return childrenById;
    }
}
