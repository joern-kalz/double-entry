package com.github.joern.kalz.doubleentry.services.accounts;

import com.github.joern.kalz.doubleentry.models.Account;
import com.github.joern.kalz.doubleentry.models.AccountsRepository;
import com.github.joern.kalz.doubleentry.services.PrincipalProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AccountsHierarchyService {

    @Autowired
    private AccountsRepository accountsRepository;

    @Autowired
    private PrincipalProvider principalProvider;

    public void walkDepthFirst(AccountsVisitor accountsVisitor) {
        List<Account> rootAccounts = getRootAccounts();

        Set<Long> knownIds = rootAccounts.stream()
                .map(Account::getId)
                .collect(Collectors.toSet());
        Deque<State> stateStack = rootAccounts.stream()
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
