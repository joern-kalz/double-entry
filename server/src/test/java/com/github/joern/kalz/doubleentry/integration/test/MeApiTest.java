package com.github.joern.kalz.doubleentry.integration.test;

import com.github.joern.kalz.doubleentry.model.AuthoritiesRepository;
import com.github.joern.kalz.doubleentry.model.Authority;
import com.github.joern.kalz.doubleentry.model.User;
import com.github.joern.kalz.doubleentry.model.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class MeApiTest {

    private static final String USERNAME = "USERNAME";
    private static final String PASSWORD = "PASSWORD";

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private AuthoritiesRepository authoritiesRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        User testUser = new User();
        testUser.setUsername(USERNAME);
        testUser.setPassword(passwordEncoder.encode(PASSWORD));
        testUser.setEnabled(true);

        User createdTestUser = usersRepository.save(testUser);
        authoritiesRepository.save(new Authority(createdTestUser, "USER"));

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .defaultRequest(get("/me").with(user(USERNAME)).with(csrf()))
                .alwaysDo(MockMvcResultHandlers.print())
                .build();
    }

    @Test
    public void shouldProvideUserInformation() throws Exception {
        mockMvc.perform(get("/me").with(user(USERNAME)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(USERNAME)));
    }

    @Test
    public void shouldUpdatePassword() throws Exception {
        String newPassword = "NEW_PASSWORD";
        String requestBody = "{\"oldPassword\":\"" + PASSWORD + "\",\"newPassword\":\"" + newPassword + "\"}";

        mockMvc.perform(patch("/me").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isNoContent());

        Optional<User> user = usersRepository.findById(USERNAME);
        assertTrue(user.isPresent());
        assertTrue(passwordEncoder.matches(newPassword, user.get().getPassword()));
    }

    @Test
    public void shouldNotUpdatePasswordIfOldPasswordIncorrect() throws Exception {
        String newPassword = "NEW_PASSWORD";
        String requestBody = "{\"oldPassword\":\"" + newPassword + "\",\"newPassword\":\"" + newPassword + "\"}";

        mockMvc.perform(patch("/me").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isBadRequest());

        Optional<User> user = usersRepository.findById(USERNAME);
        assertTrue(user.isPresent());
        assertTrue(passwordEncoder.matches(PASSWORD, user.get().getPassword()));
    }
}
