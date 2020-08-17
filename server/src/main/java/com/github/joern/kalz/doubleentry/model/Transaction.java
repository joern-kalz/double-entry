package com.github.joern.kalz.doubleentry.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Transaction {

    @Id
    @GeneratedValue
    private Long id;

    @OneToMany(mappedBy = "id.transaction")
    private List<Entry> entries;

    private LocalDate date;
    private String name;

    public Transaction() {
    }

    public Transaction(LocalDate date, String name) {
        this.date = date;
        this.name = name;
        this.entries = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
