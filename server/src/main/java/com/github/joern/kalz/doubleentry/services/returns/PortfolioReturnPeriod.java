package com.github.joern.kalz.doubleentry.services.returns;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class PortfolioReturnPeriod {
    private LocalDate start;
    private LocalDate end;
    private BigDecimal portfolioReturn;
}
