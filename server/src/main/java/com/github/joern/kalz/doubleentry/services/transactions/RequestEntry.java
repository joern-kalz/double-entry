package com.github.joern.kalz.doubleentry.services.transactions;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RequestEntry {
    private long accountId;
    private BigDecimal amount;
    private boolean verified;
}
