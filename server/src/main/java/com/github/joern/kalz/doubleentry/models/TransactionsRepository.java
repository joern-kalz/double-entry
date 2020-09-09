package com.github.joern.kalz.doubleentry.models;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TransactionsRepository extends CrudRepository<Transaction, Long>, TransactionsCustomRepository {

    List<Transaction> findByUser(User user);

    @Override
    @Query("SELECT t FROM Transaction t JOIN FETCH t.entries WHERE t.id = :id")
    Optional<Transaction> findById(@Param("id") Long id);

    @Override
    @Query("SELECT t FROM Transaction t JOIN FETCH t.entries")
    Set<Transaction> findAll();
}
