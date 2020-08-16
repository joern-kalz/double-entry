package com.github.joern.kalz.doubleentry.integration.test;

import com.github.joern.kalz.doubleentry.model.Account;
import com.github.joern.kalz.doubleentry.model.AccountsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AccountsApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountsRepository accountsRepository;

    private long rootAccountId;

    @BeforeEach
    public void resetAccounts() {
        accountsRepository.deleteAll();
        rootAccountId = accountsRepository.save(new Account(null, "root", true)).getId();
    }

    @Test
    public void shouldCreateAccount() throws Exception {
        String accountName = "cash";
        String requestBody = "{\"name\":\"" + accountName + "\",\"parentId\":" + rootAccountId + "}";

        mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(user("user"))
                .with(csrf()))
                .andExpect(status().isCreated());

        List<Account> accounts = accountsRepository.findByName(accountName);
        assertEquals(1, accounts.size());
        assertEquals(rootAccountId, accounts.get(0).getParent().getId());
    }

    @Test
    public void shouldGetAccounts() throws Exception {
        mockMvc.perform(get("/accounts")
                .with(user("user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is("root")));
    }
}
