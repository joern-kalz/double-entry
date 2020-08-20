package com.github.joern.kalz.doubleentry.models;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TransactionsRepository extends CrudRepository<Transaction, Long>, TransactionsCustomRepository {

    List<Transaction> findByUser(User user);
}
