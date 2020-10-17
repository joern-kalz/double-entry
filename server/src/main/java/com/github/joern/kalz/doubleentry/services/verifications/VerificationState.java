package com.github.joern.kalz.doubleentry.services.verifications;

import com.github.joern.kalz.doubleentry.models.Transaction;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class VerificationState {
    private BigDecimal verifiedBalance;
    private List<Transaction> unverifiedTransactions;
}
