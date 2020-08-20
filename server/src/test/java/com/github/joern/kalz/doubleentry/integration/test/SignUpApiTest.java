package com.github.joern.kalz.doubleentry.integration.test;

import com.github.joern.kalz.doubleentry.models.User;
import com.github.joern.kalz.doubleentry.models.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class SignUpApiTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UsersRepository usersRepository;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .defaultRequest(post("/sign-up")
                .contentType(MediaType.APPLICATION_JSON))
                .alwaysDo(MockMvcResultHandlers.print())
                .build();

        usersRepository.deleteAll();
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
