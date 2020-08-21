package com.github.joern.kalz.doubleentry.integration.test;

import com.github.joern.kalz.doubleentry.integration.test.setup.TestSetup;
import com.github.joern.kalz.doubleentry.integration.test.setup.TestTransactionEntry;
import com.github.joern.kalz.doubleentry.models.Account;
import com.github.joern.kalz.doubleentry.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class VerificationsApiTest {
    private MockMvc mockMvc;

    @Autowired
    private TestSetup testSetup;

    private User loggedInUser;
    private User otherUser;
    private Account foodAccount;
    private Account carAccount;
    private Account cashAccount;

    @BeforeEach
    public void setup() {
        testSetup.clearDatabase();
        loggedInUser = testSetup.createUser("LOGGED_IN_USERNAME", "");
        otherUser = testSetup.createUser("OTHER_USERNAME", "");

        mockMvc = testSetup.createAuthenticatedMockMvc(loggedInUser);

        foodAccount = testSetup.createAccount("food", loggedInUser, null);
        carAccount = testSetup.createAccount("car", loggedInUser, null);
        cashAccount = testSetup.createAccount("cash", loggedInUser, null);
    }

    @Test
    public void shouldGetVerifications() throws Exception {
        createVerifiedTransaction("supermarket", loggedInUser, foodAccount, "1.59");
        createVerifiedTransaction("beverage market", loggedInUser, foodAccount, "5.99");
        createUnverifiedTransaction("baker", loggedInUser, foodAccount, "3.79");

        mockMvc.perform(get("/verifications/" + foodAccount.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verifiedBalance", is(7.58)))
                .andExpect(jsonPath("$.unverifiedTransactions.length()", is(1)))
                .andExpect(jsonPath("$.unverifiedTransactions[0].name", is("baker")));
    }

    @Test
    public void shouldNotGetVerificationsOfOtherAccount() throws Exception {
        createVerifiedTransaction("supermarket", loggedInUser, foodAccount, "1.59");
        createUnverifiedTransaction("baker", loggedInUser, cashAccount, "3.79");

        mockMvc.perform(get("/verifications/" + foodAccount.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verifiedBalance", is(1.59)))
                .andExpect(jsonPath("$.unverifiedTransactions.length()", is(0)));
    }

    @Test
    public void shouldNotGetVerificationsOfOtherUser() throws Exception {
        createVerifiedTransaction("supermarket", loggedInUser, foodAccount, "1.59");
        createUnverifiedTransaction("baker", otherUser, foodAccount, "3.79");

        mockMvc.perform(get("/verifications/" + foodAccount.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verifiedBalance", is(1.59)))
                .andExpect(jsonPath("$.unverifiedTransactions.length()", is(0)));
    }

    public void createVerifiedTransaction(String name, User user, Account debitAccount, String debitAmount) {
        testSetup.createTransaction(name, user, LocalDate.EPOCH,
                new TestTransactionEntry(debitAccount, debitAmount, true),
                new TestTransactionEntry(cashAccount, "-" + debitAmount, true));
    }

    public void createUnverifiedTransaction(String name, User user, Account debitAccount, String debitAmount) {
        testSetup.createTransaction(name, user, LocalDate.EPOCH,
                new TestTransactionEntry(debitAccount, debitAmount, false),
                new TestTransactionEntry(cashAccount, "-" + debitAmount, false));
    }

}
