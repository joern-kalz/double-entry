package com.github.joern.kalz.doubleentry.integration.test;

import com.github.joern.kalz.doubleentry.integration.test.setup.TestSetup;
import com.github.joern.kalz.doubleentry.models.User;
import com.github.joern.kalz.doubleentry.models.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class SignUpApiTest {

    @Autowired
    private TestSetup testSetup;

    @Autowired
    private UsersRepository usersRepository;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        testSetup.clearDatabase();
        mockMvc = testSetup.createMockMvc();
    }

    @Test
    public void shouldSignUp() throws Exception {
        String requestBody = "{\"name\":\"joern\",\"password\":\"secret\"}";

        mockMvc.perform(post("/sign-up")
                .content(requestBody))
                .andExpect(status().isCreated());

        Optional<User> user = usersRepository.findById("joern");
        assertTrue(user.isPresent());
    }

    @Test
    public void shouldFailIfNameMissing() throws Exception {
        String requestBody = "{\"password\":\"secret\"}";

        mockMvc.perform(post("/sign-up")
                .content(requestBody))
                .andExpect(status().isBadRequest());

        Optional<User> user = usersRepository.findById("joern");
        assertFalse(user.isPresent());
    }

    @Test
    public void shouldFailIfUserAlreadyExists() throws Exception {
        String requestBody = "{\"name\":\"joern\",\"password\":\"secret\"}";

        mockMvc.perform(post("/sign-up")
                .content(requestBody))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/sign-up")
                .content(requestBody))
                .andExpect(status().isConflict());
    }
}
