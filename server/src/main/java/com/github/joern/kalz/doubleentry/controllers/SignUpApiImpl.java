package com.github.joern.kalz.doubleentry.controllers;

import com.github.joern.kalz.doubleentry.controllers.exceptions.AlreadyExistsException;
import com.github.joern.kalz.doubleentry.generated.api.SignUpApi;
import com.github.joern.kalz.doubleentry.generated.model.SignUpRequest;
import com.github.joern.kalz.doubleentry.model.AuthoritiesRepository;
import com.github.joern.kalz.doubleentry.model.Authority;
import com.github.joern.kalz.doubleentry.model.User;
import com.github.joern.kalz.doubleentry.model.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import javax.validation.Valid;

@RestController
public class SignUpApiImpl implements SignUpApi {

    public static final String DEFAULT_AUTHORITY = "USER";

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private AuthoritiesRepository authoritiesRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public ResponseEntity<Void> signUp(@Valid SignUpRequest signUpRequest) {
        String name = signUpRequest.getName();
        String password = signUpRequest.getPassword();

        if (usersRepository.findById(name).isPresent()) {
            throw new AlreadyExistsException("username already exists");
        }

        User createdUser = usersRepository.save(new User(name, password, true));
        authoritiesRepository.save(new Authority(createdUser, DEFAULT_AUTHORITY));

        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
