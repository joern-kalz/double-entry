package com.github.joern.kalz.doubleentry.services.user;

import com.github.joern.kalz.doubleentry.controllers.exceptions.AlreadyExistsException;
import com.github.joern.kalz.doubleentry.controllers.exceptions.ParameterException;
import com.github.joern.kalz.doubleentry.model.AuthoritiesRepository;
import com.github.joern.kalz.doubleentry.model.Authority;
import com.github.joern.kalz.doubleentry.model.User;
import com.github.joern.kalz.doubleentry.model.UsersRepository;
import com.github.joern.kalz.doubleentry.services.PrincipalProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final String DEFAULT_AUTHORITY = "USER";

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private AuthoritiesRepository authoritiesRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PrincipalProvider principalProvider;

    @Transactional
    public void create(CreateUserRequest createUserRequest) {
        String name = createUserRequest.getName();
        String password = createUserRequest.getPassword();

        if (usersRepository.findById(name).isPresent()) {
            throw new AlreadyExistsException("username already exists");
        }

        User createdUser = usersRepository.save(new User(name, password, true));
        authoritiesRepository.save(new Authority(createdUser, DEFAULT_AUTHORITY));
    }

    public User getMe() {
        return principalProvider.getPrincipal();
    }

    @Transactional
    public void updateMyPassword(String oldPassword, String newPassword) {
        User user = principalProvider.getPrincipal();

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new ParameterException("old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        usersRepository.save(user);
    }

}
