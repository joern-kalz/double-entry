package com.github.joern.kalz.doubleentry.integration.test;

import com.github.joern.kalz.doubleentry.integration.test.setup.TestSetup;
import com.github.joern.kalz.doubleentry.models.Account;
import com.github.joern.kalz.doubleentry.models.AccountsRepository;
import com.github.joern.kalz.doubleentry.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class AccountsApiTest {

    @Autowired
    private TestSetup testSetup;

    private MockMvc mockMvc;

    @Autowired
    private AccountsRepository accountsRepository;

    private User loggedInUser;
    private User otherUser;

    @BeforeEach
    public void setup() {
        testSetup.clearDatabase();
        loggedInUser = testSetup.createUser("LOGGED_IN_USERNAME");
        otherUser = testSetup.createUser("OTHER_USERNAME");
        mockMvc = testSetup.createAuthenticatedMockMvc(loggedInUser);
    }

    @Test
    public void shouldCreateAccount() throws Exception {
        Account parentAccount = testSetup.createAccount("parent", loggedInUser, null);
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
        Account parentAccount = testSetup.createAccount("parent", loggedInUser, null);
        String requestBody = "{\"name\":\"\",\"parentId\":" + parentAccount.getId() + "}";

        mockMvc.perform(post("/accounts").content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldGetAccounts() throws Exception {
        testSetup.createAccount("food", loggedInUser, null);

        mockMvc.perform(get("/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("length()", is(1)));
    }

    @Test
    public void shouldNotGetAccountsOfOtherUser() throws Exception {
        testSetup.createAccount("food", otherUser, null);

        mockMvc.perform(get("/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("length()", is(0)));
    }

    @Test
    public void shouldUpdateAccount() throws Exception {
        Account parentAccount = testSetup.createAccount("parent", loggedInUser, null);
        Account childAccount = testSetup.createAccount("lease", loggedInUser, parentAccount);
        String requestBody = "{\"name\":\"food\",\"parentId\":" + parentAccount.getId() + "}";

        mockMvc.perform(put("/accounts/" + childAccount.getId()).content(requestBody))
                .andExpect(status().isNoContent());

        Optional<Account> account = accountsRepository.findById(childAccount.getId());
        assertTrue(account.isPresent());
        assertEquals("food", account.get().getName());
    }

    @Test
    public void shouldFailIfAccountOwnedByDifferentUser() throws Exception {
        Account parentAccount = testSetup.createAccount("parent", otherUser, null);
        Account childAccount = testSetup.createAccount("lease", otherUser, parentAccount);
        String requestBody = "{\"name\":\"food\",\"parentId\":" + parentAccount.getId() + "}";

        mockMvc.perform(put("/accounts/" + childAccount.getId()).content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldFailIfParentChildRelationshipCyclic() throws Exception {
        Account parentAccount = testSetup.createAccount("parent", loggedInUser, null);
        Account childAccount = testSetup.createAccount("lease", loggedInUser, parentAccount);
        String requestBody = "{\"name\":\"food\",\"parentId\":" + childAccount.getId() + "}";

        mockMvc.perform(put("/accounts/" + parentAccount.getId()).content(requestBody))
                .andExpect(status().isBadRequest());
    }
}
