package com.github.joern.kalz.doubleentry.services.transaction;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CreateRequest {

    private LocalDate date;
    private String name;
    private List<RequestEntry> entries = new ArrayList<>();

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

    public List<RequestEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<RequestEntry> entries) {
        this.entries = entries;
    }
}
