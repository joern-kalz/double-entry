package com.github.joern.kalz.doubleentry.integration.test;

import com.github.joern.kalz.doubleentry.models.Account;
import com.github.joern.kalz.doubleentry.models.AccountsRepository;
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

    private static final String LOGGED_IN_USERNAME = "LOGGED_IN_USER";
    private static final String OTHER_USERNAME = "OTHER_USER";

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private AccountsRepository accountsRepository;

    @Autowired
    private UsersRepository usersRepository;

    private User loggedInUser;
    private User otherUser;

    @BeforeEach
    public void setup() {
        usersRepository.deleteAll();
        loggedInUser = usersRepository.save(new User(LOGGED_IN_USERNAME, null, true));
        otherUser = usersRepository.save(new User(OTHER_USERNAME, null, true));

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .defaultRequest(get("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(LOGGED_IN_USERNAME)).with(csrf()))
                .alwaysDo(MockMvcResultHandlers.print())
                .build();
    }

    @Test
    public void shouldCreateAccount() throws Exception {
        Account parentAccount = accountsRepository.save(createAccountForLoggedInUser("parent", null));
        String requestBody = "{\"name\":\"cash\",\"parentId\":" + parentAccount.getId() + "}";

        mockMvc.perform(post("/accounts").content(requestBody))
                .andExpect(status().isCreated());

        List<Account> accounts = accountsRepository.findByName("cash");
        assertEquals(1, accounts.size());
        Account account = accounts.get(0);
        assertEquals(parentAccount.getId(), account.getParent().getId());
        assertTrue(account.isActive());
    }

    @Test
    public void shouldFailIfNameBlank() throws Exception {
        Account parentAccount = accountsRepository.save(createAccountForLoggedInUser("parent", null));
        String requestBody = "{\"name\":\"\",\"parentId\":" + parentAccount.getId() + "}";

        mockMvc.perform(post("/accounts").content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldGetAccounts() throws Exception {
        accountsRepository.save(createAccountForLoggedInUser("food", null));

        mockMvc.perform(get("/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("length()", is(1)));
    }

    @Test
    public void shouldNotGetAccountsOfOtherUser() throws Exception {
        accountsRepository.save(createAccountForOtherUser("food", null));

        mockMvc.perform(get("/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("length()", is(0)));
    }

    @Test
    public void shouldUpdateAccount() throws Exception {
        Account parentAccount = accountsRepository.save(createAccountForLoggedInUser("parent", null));
        Account childAccount = accountsRepository.save(createAccountForLoggedInUser("lease", parentAccount));
        String requestBody = "{\"name\":\"food\",\"parentId\":" + parentAccount.getId() + "}";

        mockMvc.perform(put("/accounts/" + childAccount.getId()).content(requestBody))
                .andExpect(status().isNoContent());

        Optional<Account> account = accountsRepository.findById(childAccount.getId());
        assertTrue(account.isPresent());
        assertEquals("food", account.get().getName());
    }

    @Test
    public void shouldFailIfAccountOwnedByDifferentUser() throws Exception {
        Account parentAccount = accountsRepository.save(createAccountForOtherUser("parent", null));
        Account childAccount = accountsRepository.save(createAccountForOtherUser("lease", parentAccount));
        String requestBody = "{\"name\":\"food\",\"parentId\":" + parentAccount.getId() + "}";

        mockMvc.perform(put("/accounts/" + childAccount.getId()).content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldFailIfParentChildRelationshipCyclic() throws Exception {
        Account parentAccount = accountsRepository.save(createAccountForLoggedInUser("parent", null));
        Account childAccount = accountsRepository.save(createAccountForLoggedInUser("lease", parentAccount));
        String requestBody = "{\"name\":\"food\",\"parentId\":" + childAccount.getId() + "}";

        mockMvc.perform(put("/accounts/" + parentAccount.getId()).content(requestBody))
                .andExpect(status().isBadRequest());
    }

    private Account createAccountForLoggedInUser(String name, Account parent) {
        Account account = createAccount(name, parent);
        account.setUser(loggedInUser);
        return account;
    }

    private Account createAccountForOtherUser(String name, Account parent) {
        Account account = createAccount(name, parent);
        account.setUser(otherUser);
        return account;
    }

    private Account createAccount(String name, Account parent) {
        Account account = new Account();
        account.setName(name);
        account.setParent(parent);
        account.setActive(true);
        return account;
    }
}
