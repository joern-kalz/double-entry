package com.github.joern.kalz.doubleentry.services;

import com.github.joern.kalz.doubleentry.models.User;
import com.github.joern.kalz.doubleentry.models.UsersRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class PrincipalProvider {

    private final UsersRepository usersRepository;

    public PrincipalProvider(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    public User getPrincipal() {
        return usersRepository
                .findById(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new IllegalStateException("cannot find logged in user in database"));
    }
}
