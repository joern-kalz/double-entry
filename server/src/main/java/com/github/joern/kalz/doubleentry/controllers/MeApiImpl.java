package com.github.joern.kalz.doubleentry.controllers;

import com.github.joern.kalz.doubleentry.generated.api.MeApi;
import com.github.joern.kalz.doubleentry.generated.model.ApiGetMeResponse;
import com.github.joern.kalz.doubleentry.generated.model.ApiUpdateMeRequest;
import com.github.joern.kalz.doubleentry.models.User;
import com.github.joern.kalz.doubleentry.services.users.UsersService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api")
public class MeApiImpl implements MeApi {

    private final UsersService usersService;

    public MeApiImpl(UsersService usersService) {
        this.usersService = usersService;
    }

    @Override
    public ResponseEntity<ApiGetMeResponse> getMe() {
        User user = usersService.getMe();
        ApiGetMeResponse getMeResponse = new ApiGetMeResponse().name(user.getUsername());
        return new ResponseEntity<>(getMeResponse, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> updateMe(@Valid ApiUpdateMeRequest updateMeRequest) {
        usersService.updateMyPassword(updateMeRequest.getOldPassword(), updateMeRequest.getNewPassword());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
