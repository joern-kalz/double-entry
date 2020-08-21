package com.github.joern.kalz.doubleentry.services.verifications;

import com.github.joern.kalz.doubleentry.models.Transaction;

import java.math.BigDecimal;
import java.util.List;

public class VerificationState {
    private BigDecimal verifiedBalance;
    private List<Transaction> unverifiedTransactions;

    public BigDecimal getVerifiedBalance() {
        return verifiedBalance;
    }

    public void setVerifiedBalance(BigDecimal verifiedBalance) {
        this.verifiedBalance = verifiedBalance;
    }

    public List<Transaction> getUnverifiedTransactions() {
        return unverifiedTransactions;
    }

    public void setUnverifiedTransactions(List<Transaction> unverifiedTransactions) {
        this.unverifiedTransactions = unverifiedTransactions;
    }
}
