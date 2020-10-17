package com.github.joern.kalz.doubleentry.services.accounts;

import lombok.Data;

@Data
public class UpdateAccountRequest {

    private long id;
    private String name;
    private Long parentId;
    private boolean active;
}
