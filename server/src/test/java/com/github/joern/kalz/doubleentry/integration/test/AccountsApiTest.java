package com.github.joern.kalz.doubleentry.integration.test;

import com.github.joern.kalz.doubleentry.model.Account;
import com.github.joern.kalz.doubleentry.model.AccountsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
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
    private Account rootAccount;

    @BeforeEach
    public void setup() {
        accountsRepository.deleteAll();
        rootAccount = accountsRepository.save(new Account(null, "root", true));

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .defaultRequest(get("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user("joern")).with(csrf()))
                .alwaysDo(MockMvcResultHandlers.print())
                .build();
    }

    @Test
    public void shouldCreateAccount() throws Exception {
        String accountName = "cash";
        String requestBody = "{\"name\":\"" + accountName + "\",\"parentId\":" + rootAccount.getId() + "}";

        mockMvc.perform(post("/accounts").content(requestBody))
                .andExpect(status().isCreated());

        List<Account> accounts = accountsRepository.findByName(accountName);
        assertEquals(1, accounts.size());
        Account account = accounts.get(0);
        assertEquals(rootAccount.getId(), account.getParent().getId());
        assertTrue(account.isActive());
    }

    @Test
    public void shouldFailIfNameBlank() throws Exception {
        String requestBody = "{\"name\":\"\",\"parentId\":" + rootAccount.getId() + "}";

        mockMvc.perform(post("/accounts").content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldGetAccounts() throws Exception {
        mockMvc.perform(get("/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is("root")));
    }

    @Test
    public void shouldUpdateAccount() throws Exception {
        String newAccountName = "food";
        Account childAccount = accountsRepository.save(new Account(rootAccount, "lease", true));
        String requestBody = "{\"name\":\"" + newAccountName + "\",\"parentId\":" + rootAccount.getId() + "}";

        mockMvc.perform(put("/accounts/" + childAccount.getId()).content(requestBody))
                .andExpect(status().isNoContent());

        Optional<Account> account = accountsRepository.findById(childAccount.getId());
        assertTrue(account.isPresent());
        assertEquals(newAccountName, account.get().getName());
    }

    @Test
    public void shouldFailIfParentChildRelationshipCyclic() throws Exception {
        Account childAccount = accountsRepository.save(new Account(rootAccount, "lease", true));
        String requestBody = "{\"name\":\"" + rootAccount.getName() + "\",\"parentId\":" + childAccount.getId() + "}";

        mockMvc.perform(put("/accounts/" + rootAccount.getId()).content(requestBody))
                .andExpect(status().isBadRequest());
    }
}
