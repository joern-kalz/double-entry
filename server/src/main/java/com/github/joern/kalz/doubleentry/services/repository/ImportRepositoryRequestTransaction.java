package com.github.joern.kalz.doubleentry.services.repository;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ImportRepositoryRequestTransaction {
    private long id;
    private List<ImportRepositoryRequestEntry> entries;
    private LocalDate date;
    private String name;
}
