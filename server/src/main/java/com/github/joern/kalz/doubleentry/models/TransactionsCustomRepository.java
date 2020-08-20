package com.github.joern.kalz.doubleentry.models;

import java.time.LocalDate;
import java.util.Set;

public interface TransactionsCustomRepository {
    Set<Transaction> findByUserAndDateAndAccount(User user, LocalDate after, LocalDate before, Long accountId);
}
