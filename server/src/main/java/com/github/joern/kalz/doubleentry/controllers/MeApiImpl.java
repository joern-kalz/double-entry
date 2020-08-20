package com.github.joern.kalz.doubleentry.controllers;

import com.github.joern.kalz.doubleentry.generated.api.MeApi;
import com.github.joern.kalz.doubleentry.generated.model.GetMeResponse;
import com.github.joern.kalz.doubleentry.generated.model.UpdateMeRequest;
import com.github.joern.kalz.doubleentry.models.User;
import com.github.joern.kalz.doubleentry.services.users.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class MeApiImpl implements MeApi {

    @Autowired
    private UsersService usersService;

    @Override
    public ResponseEntity<GetMeResponse> getMe() {
        User user = usersService.getMe();
        GetMeResponse getMeResponse = new GetMeResponse().name(user.getUsername());
        return new ResponseEntity<>(getMeResponse, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> updateMe(@Valid UpdateMeRequest updateMeRequest) {
        usersService.updateMyPassword(updateMeRequest.getOldPassword(), updateMeRequest.getNewPassword());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
