package com.github.joern.kalz.doubleentry.services.repository;

import lombok.Data;

import java.util.List;

@Data
public class ImportRepositoryRequest {
    private List<ImportRepositoryRequestAccount> accounts;
    private List<ImportRepositoryRequestTransaction> transactions;
}
