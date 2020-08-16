package com.github.joern.kalz.doubleentry.integration.test;

import com.github.joern.kalz.doubleentry.generated.model.GetMeResponse;
import com.github.joern.kalz.doubleentry.generated.model.UpdateMeRequest;
import com.github.joern.kalz.doubleentry.model.AuthoritiesRepo;
import com.github.joern.kalz.doubleentry.model.Authority;
import com.github.joern.kalz.doubleentry.model.User;
import com.github.joern.kalz.doubleentry.model.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MeApiTest {

    private static final String USERNAME = "USERNAME";
    private static final String PASSWORD = "PASSWORD";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private AuthoritiesRepo authoritiesRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setupUser() {
        User user = new User();
        user.setUsername(USERNAME);
        user.setPassword(passwordEncoder.encode(PASSWORD));
        user.setEnabled(true);

        User createdUser = usersRepository.save(user);
        authoritiesRepo.save(new Authority(createdUser, "USER"));
    }

    @Test
    public void shouldProvideUserInformation() {
        ResponseEntity<GetMeResponse> response = restTemplate.withBasicAuth(USERNAME, PASSWORD)
                .getForEntity("/me", GetMeResponse.class);

        assertNotNull(response.getBody());
        assertEquals(USERNAME, response.getBody().getName());
    }

    @Test
    public void shouldUpdatePassword() {
        HttpHeaders sessionHeaders = establishSession();
        String newPassword = "NEW_PASSWORD";
        UpdateMeRequest updateMeRequest = new UpdateMeRequest().oldPassword(PASSWORD).newPassword(newPassword);

        ResponseEntity<Void> response = restTemplate.exchange("/me", HttpMethod.PATCH,
                new HttpEntity<>(updateMeRequest, sessionHeaders), Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Optional<User> user = usersRepository.findById(USERNAME);
        assertTrue(user.isPresent());
        assertTrue(passwordEncoder.matches(newPassword, user.get().getPassword()));
    }

    private HttpHeaders establishSession() {
        ResponseEntity<GetMeResponse> response = restTemplate.withBasicAuth(USERNAME, PASSWORD)
                .getForEntity("/me", GetMeResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        String jsessionid = extractCookieValueFromHeaders("JSESSIONID", response.getHeaders());
        String xsrfToken = extractCookieValueFromHeaders("XSRF-TOKEN", response.getHeaders());
        System.out.println(response.getHeaders());
        System.out.println(xsrfToken);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.COOKIE, "JSESSIONID=" + jsessionid + "; XSRF-TOKEN=" + xsrfToken);
        headers.set("X-XSRF-TOKEN", xsrfToken);

        return headers;
    }

    private String extractCookieValueFromHeaders(String cookieName, HttpHeaders httpHeaders) {
        List<String> cookieHeaders = httpHeaders.get(HttpHeaders.SET_COOKIE);
        assertNotNull(cookieHeaders);

        return cookieHeaders.stream()
                .filter(cookie -> cookie.startsWith(cookieName + "="))
                .map(cookie -> cookie.split("[=;]")[1])
                .findFirst()
                .orElseGet(() -> fail("cookie " + cookieName + " missing"));
    }

}
