package com.github.joern.kalz.doubleentry.model;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import java.math.BigDecimal;

@Entity
public class Entry {

    @EmbeddedId
    private EntryId id;

    private BigDecimal amount;
    private boolean verified;

    public Entry() {
    }

    public Entry(Transaction transaction, Account account, BigDecimal amount, boolean verified) {
        this.id = new EntryId(transaction, account);
        this.amount = amount;
        this.verified = verified;
    }

    public EntryId getId() {
        return id;
    }

    public void setId(EntryId id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}
