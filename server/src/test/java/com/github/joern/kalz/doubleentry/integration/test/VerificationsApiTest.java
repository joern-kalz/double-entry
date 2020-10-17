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
class VerificationsApiTest {
    MockMvc mockMvc;

    @Autowired
    TestSetup testSetup;

    @Autowired
    TransactionsRepository transactionsRepository;

    User loggedInUser;
    User otherUser;
    Account foodAccount;
    Account carAccount;
    Account cashAccount;
    Account foodAccountOfOtherUser;
    Account cashAccountOfOtherUser;

    @BeforeEach
    void setup() {
        testSetup.clearDatabase();
        loggedInUser = testSetup.createUser("LOGGED_IN_USERNAME", "");
        otherUser = testSetup.createUser("OTHER_USERNAME", "");

        mockMvc = testSetup.createAuthenticatedMockMvc(loggedInUser);

        foodAccount = testSetup.createAccount("food", loggedInUser);
        carAccount = testSetup.createAccount("car", loggedInUser);
        cashAccount = testSetup.createAccount("cash", loggedInUser);
        foodAccountOfOtherUser = testSetup.createAccount("food of other user", otherUser);
        cashAccountOfOtherUser = testSetup.createAccount("cash of other user", otherUser);
    }

    @Test
    void shouldGetVerifications() throws Exception {
        createVerifiedTransaction("supermarket", loggedInUser, foodAccount, cashAccount, "1.59");
        createVerifiedTransaction("beverage market", loggedInUser, foodAccount, cashAccount, "5.99");
        createUnverifiedTransaction("baker", loggedInUser, foodAccount, cashAccount, "3.79");

        mockMvc.perform(get("/api/verifications/" + foodAccount.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verifiedBalance", is(7.58)))
                .andExpect(jsonPath("$.unverifiedTransactions.length()", is(1)))
                .andExpect(jsonPath("$.unverifiedTransactions[0].name", is("baker")));
    }

    @Test
    void shouldNotGetVerificationsOfOtherAccount() throws Exception {
        createVerifiedTransaction("supermarket", loggedInUser, foodAccount, cashAccount, "1.59");
        createUnverifiedTransaction("baker", loggedInUser, carAccount, cashAccount, "3.79");

        mockMvc.perform(get("/api/verifications/" + foodAccount.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verifiedBalance", is(1.59)))
                .andExpect(jsonPath("$.unverifiedTransactions.length()", is(0)));
    }

    @Test
    void shouldNotGetVerificationsOfOtherUser() throws Exception {
        createUnverifiedTransaction("baker", otherUser, foodAccountOfOtherUser, cashAccountOfOtherUser,
                "3.79");

        mockMvc.perform(get("/api/verifications/" + foodAccountOfOtherUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unverifiedTransactions.length()", is(0)));
    }

    @Test
    @Transactional
    void shouldUpdateVerifications() throws Exception {
        Long id = createUnverifiedTransaction("baker", loggedInUser, foodAccount, cashAccount,
                "3.79").getId();
        String requestBody = "[" + id + "]";

        mockMvc.perform(patch("/api/verifications/" + foodAccount.getId()).content(requestBody))
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
    void shouldNotUpdateVerificationsIfTransactionNotFound() throws Exception {
        Long id = createUnverifiedTransaction("baker", loggedInUser, foodAccount, cashAccount,
                "3.79").getId();
        transactionsRepository.deleteById(id);
        String requestBody = "[" + id + "]";

        mockMvc.perform(patch("/api/verifications/" + foodAccount.getId()).content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    void shouldUpdateVerificationsOfOtherUser() throws Exception {
        Long id = createUnverifiedTransaction("baker", otherUser, foodAccount, cashAccount,
                "3.79").getId();
        String requestBody = "[" + id + "]";

        mockMvc.perform(patch("/api/verifications/" + foodAccount.getId()).content(requestBody))
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
    void shouldNotUpdateVerificationsIfNoEntryTouchesAccount() throws Exception {
        Long id = createUnverifiedTransaction("baker", loggedInUser, foodAccount, cashAccount,
                "3.79").getId();
        String requestBody = "[" + id + "]";

        mockMvc.perform(patch("/api/verifications/" + carAccount.getId()).content(requestBody))
                .andExpect(status().isBadRequest());

        Optional<Transaction> transaction = transactionsRepository.findById(id);
        assertTrue(transaction.isPresent());
        assertEquals(2, transaction.get().getEntries().size());
        assertFalse(transaction.get().getEntries().get(0).isVerified());
        assertFalse(transaction.get().getEntries().get(1).isVerified());
    }

    void createVerifiedTransaction(String name, User user, Account debitAccount, Account creditAccount,
                                          String amount) {
        testSetup.createTransaction(name, user, LocalDate.EPOCH,
                new TestTransactionEntry(debitAccount, amount, true),
                new TestTransactionEntry(creditAccount, "-" + amount, true));
    }

    Transaction createUnverifiedTransaction(String name, User user, Account debitAccount, Account creditAccount,
                                                   String amount) {
        return testSetup.createTransaction(name, user, LocalDate.EPOCH,
                new TestTransactionEntry(debitAccount, amount, false),
                new TestTransactionEntry(creditAccount, "-" + amount, false));
    }

}
