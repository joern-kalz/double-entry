package com.github.joern.kalz.doubleentry.services.repository;

import java.time.LocalDate;
import java.util.List;

public class ImportRepositoryRequestTransaction {
    private long id;
    private List<ImportRepositoryRequestEntry> entries;
    private LocalDate date;
    private String name;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<ImportRepositoryRequestEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<ImportRepositoryRequestEntry> entries) {
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
