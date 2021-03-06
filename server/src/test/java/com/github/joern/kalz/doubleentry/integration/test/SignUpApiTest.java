package com.github.joern.kalz.doubleentry.integration.test;

import com.github.joern.kalz.doubleentry.integration.test.setup.TestSetup;
import com.github.joern.kalz.doubleentry.models.User;
import com.github.joern.kalz.doubleentry.models.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class SignUpApiTest {

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
        mockMvc = testSetup.createMockMvc();
    }

    @Test
    void shouldSignUp() throws Exception {
        String requestBody = "{\"name\":\"joern\",\"password\":\"secret\"," +
                "\"repository\":{\"accounts\":[{\"id\":1,\"name\":\"root\",\"active\":true}],\"transactions\":[]}}";

        mockMvc.perform(post("/api/sign-up")
                .content(requestBody))
                .andExpect(status().isCreated());

        Optional<User> user = usersRepository.findById("joern");
        assertTrue(user.isPresent());
        assertTrue(passwordEncoder.matches("secret", user.get().getPassword()));
    }

    @Test
    void shouldFailIfNameMissing() throws Exception {
        String requestBody = "{\"password\":\"secret\"," +
                "\"repository\":{\"accounts\":[{\"id\":1,\"name\":\"root\",\"active\":true}],\"transactions\":[]}}";

        mockMvc.perform(post("/api/sign-up")
                .content(requestBody))
                .andExpect(status().isBadRequest());

        Optional<User> user = usersRepository.findById("joern");
        assertFalse(user.isPresent());
    }

    @Test
    void shouldFailIfUserAlreadyExists() throws Exception {
        String requestBody = "{\"name\":\"joern\",\"password\":\"secret\"," +
                "\"repository\":{\"accounts\":[{\"id\":1,\"name\":\"root\",\"active\":true}],\"transactions\":[]}}";

        mockMvc.perform(post("/api/sign-up")
                .content(requestBody))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/sign-up")
                .content(requestBody))
                .andExpect(status().isConflict());
    }
}
