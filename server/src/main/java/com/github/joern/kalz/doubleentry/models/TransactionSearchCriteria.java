package com.github.joern.kalz.doubleentry.models;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class TransactionSearchCriteria {
    private User user;
    private LocalDate after;
    private LocalDate before;
    private List<Long> accountIds;
    private List<Long> creditAccountIds;
    private List<Long> debitAccountIds;
    private String name;
    private Integer pageOffset;
    private Integer maxPageSize;
    private TransactionOrder order;
}
