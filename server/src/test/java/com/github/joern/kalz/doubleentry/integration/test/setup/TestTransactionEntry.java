package com.github.joern.kalz.doubleentry.integration.test.setup;

import com.github.joern.kalz.doubleentry.models.Account;

public class TestTransactionEntry {
    private final Account account;
    private final String amount;
    private final boolean verified;

    public TestTransactionEntry(Account account, String amount, boolean verified) {
        this.account = account;
        this.amount = amount;
        this.verified = verified;
    }

    public Account getAccount() {
        return account;
    }

    public String getAmount() {
        return amount;
    }

    public boolean isVerified() {
        return verified;
    }
}
