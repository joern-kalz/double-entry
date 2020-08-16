package com.github.joern.kalz.doubleentry.model;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AccountsRepository extends CrudRepository<Account, Long> {

    List<Account> findByName(String name);
}
