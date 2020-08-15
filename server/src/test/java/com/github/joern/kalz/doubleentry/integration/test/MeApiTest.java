package com.github.joern.kalz.doubleentry.integration.test;

import com.github.joern.kalz.doubleentry.generated.model.GetMeResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MeApiTest {

    @Autowired
    private TestRestTemplateBuilder testRestTemplateBuilder;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    public void shouldProvideUserInformation() {
        ResponseEntity<GetMeResponse> response = testRestTemplateBuilder.buildForUser("joern")
                .getForEntity("/me", GetMeResponse.class);
        assertNotNull(response.getBody());
        assertEquals("joern", response.getBody().getName());
    }
}
