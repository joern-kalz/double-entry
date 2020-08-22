package com.github.joern.kalz.doubleentry.services.repository;

import com.github.joern.kalz.doubleentry.models.Account;
import com.github.joern.kalz.doubleentry.models.Transaction;

import java.util.List;

public class GetRepositoryResponse {
    private List<Account> accounts;
    private List<Transaction> transactions;

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
}
