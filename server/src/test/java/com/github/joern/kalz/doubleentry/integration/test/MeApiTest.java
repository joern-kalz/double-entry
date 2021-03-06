package com.github.joern.kalz.doubleentry.integration.test;

import com.github.joern.kalz.doubleentry.integration.test.setup.TestSetup;
import com.github.joern.kalz.doubleentry.models.User;
import com.github.joern.kalz.doubleentry.models.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class MeApiTest {

    static final String USERNAME = "USERNAME";
    static final String PASSWORD = "PASSWORD";

    @Autowired
    TestSetup testSetup;

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    MockMvc mockMvc;

    @BeforeEach
    void setup() {
        testSetup.clearDatabase();
        User user = testSetup.createUser(USERNAME, PASSWORD);
        mockMvc = testSetup.createAuthenticatedMockMvc(user);
    }

    @Test
    void shouldProvideUserInformation() throws Exception {
        mockMvc.perform(get("/api/me").with(user(USERNAME)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(USERNAME)));
    }

    @Test
    void shouldUpdatePassword() throws Exception {
        String newPassword = "NEW_PASSWORD";
        String requestBody = "{\"oldPassword\":\"" + PASSWORD + "\",\"newPassword\":\"" + newPassword + "\"}";

        mockMvc.perform(patch("/api/me").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isNoContent());

        Optional<User> user = usersRepository.findById(USERNAME);
        assertTrue(user.isPresent());
        assertTrue(passwordEncoder.matches(newPassword, user.get().getPassword()));
    }

    @Test
    void shouldNotUpdatePasswordIfOldPasswordIncorrect() throws Exception {
        String newPassword = "NEW_PASSWORD";
        String requestBody = "{\"oldPassword\":\"" + newPassword + "\",\"newPassword\":\"" + newPassword + "\"}";

        mockMvc.perform(patch("/api/me").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isBadRequest());

        Optional<User> user = usersRepository.findById(USERNAME);
        assertTrue(user.isPresent());
        assertTrue(passwordEncoder.matches(PASSWORD, user.get().getPassword()));
    }
}
