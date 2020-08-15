package com.github.joern.kalz.doubleentry.controllers;

import com.github.joern.kalz.doubleentry.controllers.exceptions.AlreadyExistsException;
import com.github.joern.kalz.doubleentry.controllers.exceptions.ParameterException;
import com.github.joern.kalz.doubleentry.generated.api.SignUpApi;
import com.github.joern.kalz.doubleentry.generated.model.SignUpRequest;
import com.github.joern.kalz.doubleentry.model.*;
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
    private AuthoritiesRepo authoritiesRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public ResponseEntity<Void> signUp(@Valid SignUpRequest signUpRequest) {

        String name = signUpRequest.getName();
        String password = signUpRequest.getPassword();

        if (name == null || name.length() < 1) {
            throw new ParameterException("missing name");
        }

        if (password == null || password.length() < 1) {
            throw new ParameterException("missing userpassword");
        }

        User user = new User();
        user.setUsername(name);
        user.setPassword(passwordEncoder.encode(password));
        user.setEnabled(true);

        createUser(user);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Transactional
    public void createUser(User user) {
        if (usersRepository.findById(user.getUsername()).isPresent()) {
            throw new AlreadyExistsException("username already exists");
        }

        User createdUser = usersRepository.save(user);
        authoritiesRepo.save(new Authority(createdUser, DEFAULT_AUTHORITY));
    }
}
