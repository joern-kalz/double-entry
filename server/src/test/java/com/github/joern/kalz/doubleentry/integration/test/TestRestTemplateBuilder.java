package com.github.joern.kalz.doubleentry.integration.test;

import com.github.joern.kalz.doubleentry.generated.model.SignUpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.stereotype.Service;

@Service
public class TestRestTemplateBuilder {

    private static final String PASSWORD = "PASSWORD";

    @Autowired
    private TestRestTemplate restTemplate;

    public TestRestTemplate buildForUser(String name) {
        SignUpRequest signUpRequest = new SignUpRequest().name(name).password(PASSWORD);
        restTemplate.postForEntity("/sign-up", signUpRequest, Void.class);
        return restTemplate.withBasicAuth(name, PASSWORD);
    }
}
