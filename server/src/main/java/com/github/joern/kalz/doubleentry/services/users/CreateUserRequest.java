package com.github.joern.kalz.doubleentry.services.users;

import com.github.joern.kalz.doubleentry.services.repository.ImportRepositoryRequest;

public class CreateUserRequest {
    private String name;
    private String password;
    private ImportRepositoryRequest importRepositoryRequest;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ImportRepositoryRequest getImportRepositoryRequest() {
        return importRepositoryRequest;
    }

    public void setImportRepositoryRequest(ImportRepositoryRequest importRepositoryRequest) {
        this.importRepositoryRequest = importRepositoryRequest;
    }
}
