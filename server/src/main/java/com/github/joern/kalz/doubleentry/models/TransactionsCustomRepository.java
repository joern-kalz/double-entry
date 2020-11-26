package com.github.joern.kalz.doubleentry.models;

import java.util.List;

public interface TransactionsCustomRepository {
    List<Transaction> find(TransactionSearchCriteria request);
}
