package com.github.joern.kalz.doubleentry.integration.test;

import com.github.joern.kalz.doubleentry.integration.test.setup.TestSetup;
import com.github.joern.kalz.doubleentry.integration.test.setup.TestTransactionEntry;
import com.github.joern.kalz.doubleentry.models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class VerificationsApiTest {
    private MockMvc mockMvc;

    @Autowired
    private TestSetup testSetup;

    @Autowired
    private TransactionsRepository transactionsRepository;

    private User loggedInUser;
    private User otherUser;
    private Account foodAccount;
    private Account carAccount;
    private Account cashAccount;
    private Account foodAccountOfOtherUser;

    @BeforeEach
    public void setup() {
        testSetup.clearDatabase();
        loggedInUser = testSetup.createUser("LOGGED_IN_USERNAME", "");
        otherUser = testSetup.createUser("OTHER_USERNAME", "");

        mockMvc = testSetup.createAuthenticatedMockMvc(loggedInUser);

        foodAccount = testSetup.createAccount("food", loggedInUser, null);
        carAccount = testSetup.createAccount("car", loggedInUser, null);
        cashAccount = testSetup.createAccount("cash", loggedInUser, null);
        foodAccountOfOtherUser = testSetup.createAccount("food of other user", otherUser, null);
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
        createUnverifiedTransaction("baker", loggedInUser, carAccount, "3.79");

        mockMvc.perform(get("/verifications/" + foodAccount.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verifiedBalance", is(1.59)))
                .andExpect(jsonPath("$.unverifiedTransactions.length()", is(0)));
    }

    @Test
    public void shouldNotGetVerificationsOfOtherUser() throws Exception {
        createUnverifiedTransaction("baker", otherUser, foodAccountOfOtherUser, "3.79");

        mockMvc.perform(get("/verifications/" + foodAccountOfOtherUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unverifiedTransactions.length()", is(0)));
    }

    @Test
    @Transactional
    public void shouldUpdateVerifications() throws Exception {
        Long id = createUnverifiedTransaction("baker", loggedInUser, foodAccount,
                "3.79").getId();
        String requestBody = "[" + id + "]";

        mockMvc.perform(patch("/verifications/" + foodAccount.getId()).content(requestBody))
                .andExpect(status().isNoContent());

        Optional<Transaction> transaction = transactionsRepository.findById(id);
        assertTrue(transaction.isPresent());
        Optional<Entry> foodEntry = transaction.get().getEntries().stream()
                .filter(entry -> entry.getId().getAccount().getId().equals(foodAccount.getId()))
                .findFirst();
        assertTrue(foodEntry.isPresent());
        assertTrue(foodEntry.get().isVerified());
    }

    @Test
    @Transactional
    public void shouldNotUpdateVerificationsIfTransactionNotFound() throws Exception {
        Long id = createUnverifiedTransaction("baker", loggedInUser, foodAccount,
                "3.79").getId();
        transactionsRepository.deleteById(id);
        String requestBody = "[" + id + "]";

        mockMvc.perform(patch("/verifications/" + foodAccount.getId()).content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    public void shouldUpdateVerificationsOfOtherUser() throws Exception {
        Long id = createUnverifiedTransaction("baker", otherUser, foodAccount,
                "3.79").getId();
        String requestBody = "[" + id + "]";

        mockMvc.perform(patch("/verifications/" + foodAccount.getId()).content(requestBody))
                .andExpect(status().isBadRequest());

        Optional<Transaction> transaction = transactionsRepository.findById(id);
        assertTrue(transaction.isPresent());
        Optional<Entry> foodEntry = transaction.get().getEntries().stream()
                .filter(entry -> entry.getId().getAccount().getId().equals(foodAccount.getId()))
                .findFirst();
        assertTrue(foodEntry.isPresent());
        assertFalse(foodEntry.get().isVerified());
    }

    @Test
    @Transactional
    public void shouldNotUpdateVerificationsIfNoEntryTouchesAccount() throws Exception {
        Long id = createUnverifiedTransaction("baker", loggedInUser, foodAccount,
                "3.79").getId();
        String requestBody = "[" + id + "]";

        mockMvc.perform(patch("/verifications/" + carAccount.getId()).content(requestBody))
                .andExpect(status().isBadRequest());

        Optional<Transaction> transaction = transactionsRepository.findById(id);
        assertTrue(transaction.isPresent());
        assertEquals(2, transaction.get().getEntries().size());
        assertFalse(transaction.get().getEntries().get(0).isVerified());
        assertFalse(transaction.get().getEntries().get(1).isVerified());
    }

    public void createVerifiedTransaction(String name, User user, Account debitAccount, String debitAmount) {
        testSetup.createTransaction(name, user, LocalDate.EPOCH,
                new TestTransactionEntry(debitAccount, debitAmount, true),
                new TestTransactionEntry(cashAccount, "-" + debitAmount, true));
    }

    public Transaction createUnverifiedTransaction(String name, User user, Account debitAccount, String debitAmount) {
        return testSetup.createTransaction(name, user, LocalDate.EPOCH,
                new TestTransactionEntry(debitAccount, debitAmount, false),
                new TestTransactionEntry(cashAccount, "-" + debitAmount, false));
    }

}
