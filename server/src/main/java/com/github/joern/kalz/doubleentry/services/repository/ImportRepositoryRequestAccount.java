package com.github.joern.kalz.doubleentry.services.repository;

import lombok.Data;

@Data
public class ImportRepositoryRequestAccount {
    private long id;
    private String name;
    private Long parentId;
    private boolean active;
}
