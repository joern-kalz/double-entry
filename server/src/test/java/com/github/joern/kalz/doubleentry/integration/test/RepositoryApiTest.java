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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class RepositoryApiTest {
    MockMvc mockMvc;

    @Autowired
    TestSetup testSetup;

    @Autowired
    TransactionsRepository transactionsRepository;

    @Autowired
    AccountsRepository accountsRepository;

    User loggedInUser;
    User otherUser;

    @BeforeEach
    void setup() {
        testSetup.clearDatabase();
        loggedInUser = testSetup.createUser("LOGGED_IN_USERNAME", "");
        otherUser = testSetup.createUser("OTHER_USERNAME", "");
        mockMvc = testSetup.createAuthenticatedMockMvc(loggedInUser);
    }

    @Test
    void shouldGetRepository() throws Exception {
        Account foodAccount = testSetup.createAccount("food", loggedInUser);
        Account expenseAccount = testSetup.createAccount("expense", loggedInUser);
        testSetup.createParentChildLink(expenseAccount, foodAccount);
        Account cashAccount = testSetup.createAccount("cash", loggedInUser);
        Transaction transaction = testSetup.createTransaction("baker", loggedInUser, LocalDate.EPOCH,
                new TestTransactionEntry(foodAccount, "2.50", false),
                new TestTransactionEntry(cashAccount, "-2.50", false));

        mockMvc.perform(get("/api/repository"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accounts.length()", is(3)))
                .andExpect(jsonPath(buildAccountSearch(expenseAccount)).exists())
                .andExpect(jsonPath(buildAccountSearch(foodAccount)).exists())
                .andExpect(jsonPath(buildAccountSearch(cashAccount)).exists())
                .andExpect(jsonPath("$.transactions.length()", is(1)))
                .andExpect(jsonPath(buildTransactionSearch(transaction)).exists());
    }

    @Test
    void shouldGetRepositoryWithoutAccountsOfOtherUser() throws Exception {
        Account foodAccountOfOtherUser = testSetup.createAccount("food of other user", otherUser);
        Account cashAccountOfOtherUser = testSetup.createAccount("cash of other user", otherUser);

        mockMvc.perform(get("/api/repository"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(buildAccountSearch(foodAccountOfOtherUser)).doesNotExist())
                .andExpect(jsonPath(buildAccountSearch(cashAccountOfOtherUser)).doesNotExist());
    }

    @Test
    void shouldGetRepositoryWithoutTransactionsOfOtherUser() throws Exception {
        Account foodAccountOfOtherUser = testSetup.createAccount("food of other user", otherUser);
        Account cashAccountOfOtherUser = testSetup.createAccount("cash of other user", otherUser);
        Transaction transaction = testSetup.createTransaction("baker", otherUser, LocalDate.EPOCH,
                new TestTransactionEntry(foodAccountOfOtherUser, "2.50", false),
                new TestTransactionEntry(cashAccountOfOtherUser, "-2.50", false));

        mockMvc.perform(get("/api/repository"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(buildTransactionSearch(transaction)).doesNotExist());
    }

    @Test
    @Transactional
    void shouldRestoreAccountsFromRepository() throws Exception {
        Account foodAccount = testSetup.createAccount("food", loggedInUser);
        Account expenseAccount = testSetup.createAccount("expense", loggedInUser);
        testSetup.createParentChildLink(expenseAccount, foodAccount);
        testSetup.createAccount("cash", loggedInUser);
        String repository = mockMvc.perform(get("/api/repository"))
                .andReturn().getResponse().getContentAsString();
        accountsRepository.deleteAll();

        mockMvc.perform(post("/api/repository").content(repository));

        assertEquals(3, accountsRepository.findByUser(loggedInUser).size());
        assertTrue(isAccountRestored("cash", loggedInUser, null));
        assertTrue(isAccountRestored("expense", loggedInUser, null));
        assertTrue(isAccountRestored("food", loggedInUser, "expense"));
    }

    @Test
    @Transactional
    void shouldRestoreTransactionFromRepository() throws Exception {
        Account foodAccount = testSetup.createAccount("food", loggedInUser);
        Account cashAccount = testSetup.createAccount("cash", loggedInUser);
        testSetup.createTransaction("baker", loggedInUser, LocalDate.EPOCH,
                new TestTransactionEntry(foodAccount, "2.39", true),
                new TestTransactionEntry(cashAccount, "-2.39", false));
        String repository = mockMvc.perform(get("/api/repository"))
                .andReturn().getResponse().getContentAsString();
        transactionsRepository.deleteAll();
        accountsRepository.deleteAll();

        mockMvc.perform(post("/api/repository").content(repository));

        Set<Transaction> transactions = transactionsRepository.findByUser(loggedInUser);
        assertEquals(1, transactions.size());
        Transaction transaction = transactions.iterator().next();
        assertEquals("baker", transaction.getName());
        assertEquals(LocalDate.EPOCH, transaction.getDate());
        assertTrue(isEntryListEquivalent(transaction.getEntries(),
                new TestTransactionEntry(foodAccount, "2.39", true),
                new TestTransactionEntry(cashAccount, "-2.39", false)));

    }

    String buildAccountSearch(Account account) {
        return "$.accounts[?(@.name == '" + account.getName() + "'" +
                " && @.parentId == " + (account.getParent() != null ? account.getParent().getId() : "null") +
                " && @.active == " + (account.isActive() ? "true" : "false") + ")]";
    }

    String buildTransactionSearch(Transaction transaction) {
        return "$.transactions[?(@.name == '" + transaction.getName() + "'" +
                " && @.date == '" + transaction.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "'" +
                " && @.entries.length() == " + transaction.getEntries().size() + ")]";
    }

    boolean isAccountRestored(String name, User user, String parentName) {
        return StreamSupport.stream(accountsRepository.findAll().spliterator(), false)
                .anyMatch(account -> account.getName().equals(name) &&
                        account.getUser().getUsername().equals(user.getUsername()) &&
                        isParentName(account, parentName));
    }

    boolean isParentName(Account account, String parentName) {
        return (parentName == null && account.getParent() == null) ||
                account.getParent().getName().equals(parentName);
    }

    boolean isEntryListEquivalent(List<Entry> actualEntries, TestTransactionEntry... expectedEntries) {
        if (actualEntries.size() != expectedEntries.length) {
            return false;
        }

        for (TestTransactionEntry expectedEntry : expectedEntries) {
            boolean foundEquivalent = actualEntries.stream()
                    .anyMatch(actualEntry -> isEntryEquivalent(actualEntry, expectedEntry));

            if (!foundEquivalent) {
                return false;
            }
        }

        return true;
    }

    boolean isEntryEquivalent(Entry actualEntry, TestTransactionEntry expectedEntry) {
        return actualEntry.getId().getAccount().getName().equals(expectedEntry.getAccount().getName()) &&
                actualEntry.getAmount().equals(new BigDecimal(expectedEntry.getAmount())) &&
                actualEntry.isVerified() == expectedEntry.isVerified();
    }
}
