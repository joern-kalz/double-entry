package com.github.joern.kalz.doubleentry.services.repository;

import java.util.List;

public class ImportRepositoryRequest {
    private List<ImportRepositoryRequestAccount> accounts;
    private List<ImportRepositoryRequestTransaction> transactions;

    public List<ImportRepositoryRequestAccount> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<ImportRepositoryRequestAccount> accounts) {
        this.accounts = accounts;
    }

    public List<ImportRepositoryRequestTransaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<ImportRepositoryRequestTransaction> transactions) {
        this.transactions = transactions;
    }
}
