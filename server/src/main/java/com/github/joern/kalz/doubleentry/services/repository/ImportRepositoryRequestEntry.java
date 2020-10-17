package com.github.joern.kalz.doubleentry.services.repository;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ImportRepositoryRequestEntry {
    private long accountId;
    private BigDecimal amount;
    private boolean verified;
}
