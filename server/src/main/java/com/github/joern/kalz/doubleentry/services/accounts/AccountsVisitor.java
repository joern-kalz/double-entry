package com.github.joern.kalz.doubleentry.services.accounts;

import com.github.joern.kalz.doubleentry.models.Account;

@FunctionalInterface
public interface AccountsVisitor {

    void visit(Account account);
}
