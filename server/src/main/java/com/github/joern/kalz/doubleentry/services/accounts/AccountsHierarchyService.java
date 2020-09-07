package com.github.joern.kalz.doubleentry.services.accounts;

import com.github.joern.kalz.doubleentry.models.Account;
import com.github.joern.kalz.doubleentry.models.AccountsRepository;
import com.github.joern.kalz.doubleentry.services.PrincipalProvider;
import com.github.joern.kalz.doubleentry.services.exceptions.ParameterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AccountsHierarchyService {

    @Autowired
    private AccountsRepository accountsRepository;

    @Autowired
    private PrincipalProvider principalProvider;

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

    public void walkHierarchyDepthFirst(AccountsVisitor accountsVisitor) {
        walkDepthFirst(getRootAccounts(), accountsVisitor);
    }

    private void walkDepthFirst(List<Account> startingPoints, AccountsVisitor accountsVisitor) {
        Set<Long> knownIds = startingPoints.stream()
                .map(Account::getId)
                .collect(Collectors.toSet());
        Deque<State> stateStack = startingPoints.stream()
                .map(State::new)
                .collect(Collectors.toCollection(ArrayDeque::new));

        while (stateStack.size() > 0) {
            State state = stateStack.getLast();
            List<Account> children = state.account.getChildren();

            while (state.childIndex < children.size()) {
                Account child = children.get(state.childIndex);

                if (!knownIds.contains(child.getId())) {
                    knownIds.add(child.getId());
                    stateStack.addLast(new State(child));
                    break;
                }

                state.childIndex++;
            }

            if (state.childIndex >= children.size()) {
                accountsVisitor.visit(state.account);
                stateStack.removeLast();
            }
        }
    }

    private List<Account> getRootAccounts() {
        List<Account> rootAccounts = new ArrayList<>();

        for (Account account : accountsRepository.findByUser(principalProvider.getPrincipal())) {
            if (account.getParent() == null) {
                rootAccounts.add(account);
            }
        }

        return rootAccounts;
    }

    private static class State {
        Account account;
        int childIndex;

        public State(Account account) {
            this.account = account;
            this.childIndex = 0;
        }
    }
}
