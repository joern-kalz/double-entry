package com.github.joern.kalz.doubleentry.tests;

import com.github.joern.kalz.doubleentry.generated.model.ErrorResponse;
import com.github.joern.kalz.doubleentry.generated.model.SignUpRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SignUpApiTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void shouldSignUp() {
        SignUpRequest request = new SignUpRequest().name("joern").password("secret");
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity("/sign-up", request, ErrorResponse.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void shouldFailIfNameMissing() {
        SignUpRequest request = new SignUpRequest().password("secret");
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity("/sign-up", request, ErrorResponse.class);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void shouldFailIfPasswordMissing() {
        SignUpRequest request = new SignUpRequest().name("joern");
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity("/sign-up", request, ErrorResponse.class);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void shouldFailIfUserAlreadyExists() {
        SignUpRequest request = new SignUpRequest().name("joern").password("secret");
        restTemplate.postForEntity("/sign-up", request, ErrorResponse.class);
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity("/sign-up", request, ErrorResponse.class);
        Assertions.assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }
}
