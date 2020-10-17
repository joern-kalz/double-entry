package com.github.joern.kalz.doubleentry.services.repository;

import com.github.joern.kalz.doubleentry.models.Account;
import com.github.joern.kalz.doubleentry.models.Transaction;
import lombok.Data;

import java.util.List;

@Data
public class GetRepositoryResponse {
    private List<Account> accounts;
    private List<Transaction> transactions;
}
