package com.github.joern.kalz.doubleentry.model;

import org.springframework.data.repository.CrudRepository;

public interface TransactionsRepository extends CrudRepository<Transaction, Long> {
}
