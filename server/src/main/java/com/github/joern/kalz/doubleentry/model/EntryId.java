package com.github.joern.kalz.doubleentry.model;

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class EntryId implements Serializable {

    @ManyToOne
    private Transaction transaction;

    @ManyToOne
    private Account account;

    public EntryId() {
    }

    public EntryId(Transaction transaction, Account account) {
        this.transaction = transaction;
        this.account = account;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntryId entryId = (EntryId) o;
        return Objects.equals(transaction, entryId.transaction) &&
                Objects.equals(account, entryId.account);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transaction, account);
    }
}
