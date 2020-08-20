package com.github.joern.kalz.doubleentry.services.transactions;

import java.math.BigDecimal;

public class RequestEntry {
    private long accountId;
    private BigDecimal amount;
    private boolean verified;

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public boolean getVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}
