package com.github.joern.kalz.doubleentry.services.users;

import com.github.joern.kalz.doubleentry.models.*;
import com.github.joern.kalz.doubleentry.services.PrincipalProvider;
import com.github.joern.kalz.doubleentry.services.exceptions.AlreadyExistsException;
import com.github.joern.kalz.doubleentry.services.exceptions.ParameterException;
import com.github.joern.kalz.doubleentry.services.repository.RepositoryService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsersService {

    private static final String DEFAULT_AUTHORITY = "USER";

    private final UsersRepository usersRepository;
    private final AuthoritiesRepository authoritiesRepository;
    private final PasswordEncoder passwordEncoder;
    private final PrincipalProvider principalProvider;
    private final RepositoryService repositoryService;

    public UsersService(UsersRepository usersRepository, AuthoritiesRepository authoritiesRepository,
                        PasswordEncoder passwordEncoder, PrincipalProvider principalProvider,
                        RepositoryService repositoryService) {
        this.usersRepository = usersRepository;
        this.authoritiesRepository = authoritiesRepository;
        this.passwordEncoder = passwordEncoder;
        this.principalProvider = principalProvider;
        this.repositoryService = repositoryService;
    }

    @Transactional
    public void create(CreateUserRequest createUserRequest) {
        String name = createUserRequest.getName();

        if (usersRepository.findById(name).isPresent()) {
            throw new AlreadyExistsException("username already exists");
        }

        User user = new User();
        user.setUsername(name);
        user.setPassword(passwordEncoder.encode(createUserRequest.getPassword()));
        user.setEnabled(true);
        User createdUser = usersRepository.save(user);

        AuthorityId authorityId = new AuthorityId();
        authorityId.setAuthority(DEFAULT_AUTHORITY);
        authorityId.setUser(createdUser);
        Authority authority = new Authority();
        authority.setId(authorityId);
        authoritiesRepository.save(authority);

        repositoryService.importRepository(createdUser, createUserRequest.getImportRepositoryRequest());
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
