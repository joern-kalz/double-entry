package com.github.joern.kalz.doubleentry.integration.test;

import com.github.joern.kalz.doubleentry.model.Account;
import com.github.joern.kalz.doubleentry.model.AccountsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class AccountsApiTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private AccountsRepository accountsRepository;

    private MockMvc mockMvc;
    private long rootAccountId;

    @BeforeEach
    public void setup() {
        accountsRepository.deleteAll();
        rootAccountId = accountsRepository.save(new Account(null, "root", true)).getId();

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .defaultRequest(get("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user("joern")).with(csrf()))
                .build();
    }

    @Test
    public void shouldCreateAccount() throws Exception {
        String accountName = "cash";
        String requestBody = "{\"name\":\"" + accountName + "\",\"parentId\":" + rootAccountId + "}";

        mockMvc.perform(post("/accounts").content(requestBody))
                .andExpect(status().isCreated());

        List<Account> accounts = accountsRepository.findByName(accountName);
        assertEquals(1, accounts.size());
        assertEquals(rootAccountId, accounts.get(0).getParent().getId());
    }

    @Test
    public void shouldGetAccounts() throws Exception {
        mockMvc.perform(get("/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is("root")));
    }

    @Test
    public void shouldUpdateAccount() throws Exception {
        String newAccountName = "grocery";
        String requestBody = "{\"name\":\"" + newAccountName + "\"}";

        mockMvc.perform(patch("/accounts/" + rootAccountId).content(requestBody))
                .andExpect(status().isNoContent());

        Optional<Account> account = accountsRepository.findById(rootAccountId);
        assertTrue(account.isPresent());
        assertEquals(newAccountName, account.get().getName());
    }
}
