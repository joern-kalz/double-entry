package com.github.joern.kalz.doubleentry.services.transactions;

import com.github.joern.kalz.doubleentry.models.TransactionOrder;
import lombok.Data;

import java.time.LocalDate;

@Data
public class FindTransactionsRequest {
    private LocalDate after;
    private LocalDate before;
    private Long accountId;
    private Long creditAccountId;
    private Long debitAccountId;
    private String name;
    private Integer pageOffset;
    private Integer maxPageSize;
    private TransactionOrder order;
}
