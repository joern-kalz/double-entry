package com.github.joern.kalz.doubleentry.services;

import com.github.joern.kalz.doubleentry.model.User;
import com.github.joern.kalz.doubleentry.model.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class PrincipalProvider {

    @Autowired
    private UsersRepository usersRepository;

    public User getPrincipal() {
        return usersRepository
                .findById(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new IllegalStateException("cannot find logged in user in database"));
    }
}
