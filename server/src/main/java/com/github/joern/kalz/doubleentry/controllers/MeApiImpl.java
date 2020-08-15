package com.github.joern.kalz.doubleentry.controllers;

import com.github.joern.kalz.doubleentry.generated.api.MeApi;
import com.github.joern.kalz.doubleentry.generated.model.GetMeResponse;
import com.github.joern.kalz.doubleentry.generated.model.UpdateMeRequest;
import com.github.joern.kalz.doubleentry.services.AuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class MeApiImpl implements MeApi {

    @Autowired
    private AuthenticationProvider authenticationProvider;

    @Override
    public ResponseEntity<GetMeResponse> getMe() {
        String name = authenticationProvider.getAuthentication().getName();
        return new ResponseEntity<>(new GetMeResponse().name(name), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> updateMe(@Valid UpdateMeRequest updateMeRequest) {
        return null;
    }
}