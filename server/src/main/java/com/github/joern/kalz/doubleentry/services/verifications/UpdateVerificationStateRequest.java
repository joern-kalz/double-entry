package com.github.joern.kalz.doubleentry.services.verifications;

import java.util.List;

public class UpdateVerificationStateRequest {
    private Long accountId;
    private List<Long> transactionIds;

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public List<Long> getTransactionIds() {
        return transactionIds;
    }

    public void setTransactionIds(List<Long> transactionIds) {
        this.transactionIds = transactionIds;
    }
}
