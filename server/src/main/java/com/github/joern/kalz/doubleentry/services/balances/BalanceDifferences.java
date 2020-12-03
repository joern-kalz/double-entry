package com.github.joern.kalz.doubleentry.services.balances;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Data
@AllArgsConstructor
public class BalanceDifferences {
    private LocalDate start;
    private LocalDate end;
    private Map<Long, BigDecimal> amountsByAccountId;
}
