package com.github.joern.kalz.doubleentry.controllers;

import com.github.joern.kalz.doubleentry.controllers.exceptions.ParameterException;
import com.github.joern.kalz.doubleentry.generated.api.MeApi;
import com.github.joern.kalz.doubleentry.generated.model.GetMeResponse;
import com.github.joern.kalz.doubleentry.generated.model.UpdateMeRequest;
import com.github.joern.kalz.doubleentry.model.User;
import com.github.joern.kalz.doubleentry.model.UsersRepository;
import com.github.joern.kalz.doubleentry.services.PrincipalProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class MeApiImpl implements MeApi {

    @Autowired
    private PrincipalProvider principalProvider;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public ResponseEntity<GetMeResponse> getMe() {
        String name = principalProvider.getPrincipal().getUsername();
        return new ResponseEntity<>(new GetMeResponse().name(name), HttpStatus.OK);
    }

    @Override
    @Transactional
    public ResponseEntity<Void> updateMe(@Valid UpdateMeRequest updateMeRequest) {
        User user = principalProvider.getPrincipal();

        if (!passwordEncoder.matches(updateMeRequest.getOldPassword(), user.getPassword())) {
            throw new ParameterException("old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(updateMeRequest.getNewPassword()));
        usersRepository.save(user);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
