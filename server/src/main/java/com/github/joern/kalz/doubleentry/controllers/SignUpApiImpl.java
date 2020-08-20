package com.github.joern.kalz.doubleentry.controllers;

import com.github.joern.kalz.doubleentry.generated.api.SignUpApi;
import com.github.joern.kalz.doubleentry.generated.model.SignUpRequest;
import com.github.joern.kalz.doubleentry.services.user.CreateUserRequest;
import com.github.joern.kalz.doubleentry.services.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class SignUpApiImpl implements SignUpApi {

    @Autowired
    private UserService userService;

    @Override
    public ResponseEntity<Void> signUp(@Valid SignUpRequest signUpRequest) {
        CreateUserRequest createUserRequest = createCreateUserRequest(signUpRequest);
        userService.create(createUserRequest);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    private CreateUserRequest createCreateUserRequest(@Valid SignUpRequest signUpRequest) {
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setName(signUpRequest.getName());
        createUserRequest.setPassword(signUpRequest.getPassword());
        return createUserRequest;
    }
}
