package com.github.joern.kalz.doubleentry.controllers;

import com.github.joern.kalz.doubleentry.generated.api.SignUpApi;
import com.github.joern.kalz.doubleentry.generated.model.ApiSignUpRequest;
import com.github.joern.kalz.doubleentry.services.users.CreateUserRequest;
import com.github.joern.kalz.doubleentry.services.users.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api")
public class SignUpApiImpl implements SignUpApi {

    @Autowired
    private UsersService usersService;

    @Autowired
    private RequestFactory requestFactory;

    @Override
    public ResponseEntity<Void> signUp(@Valid ApiSignUpRequest signUpRequest) {
        CreateUserRequest createUserRequest = createCreateUserRequest(signUpRequest);
        usersService.create(createUserRequest);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    private CreateUserRequest createCreateUserRequest(@Valid ApiSignUpRequest signUpRequest) {
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setName(signUpRequest.getName());
        createUserRequest.setPassword(signUpRequest.getPassword());
        createUserRequest.setImportRepositoryRequest(requestFactory.convertToRequest(signUpRequest.getRepository()));
        return createUserRequest;
    }
}
