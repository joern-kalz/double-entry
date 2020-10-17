package com.github.joern.kalz.doubleentry.services.users;

import com.github.joern.kalz.doubleentry.services.repository.ImportRepositoryRequest;
import lombok.Data;

@Data
public class CreateUserRequest {
    private String name;
    private String password;
    private ImportRepositoryRequest importRepositoryRequest;
}
