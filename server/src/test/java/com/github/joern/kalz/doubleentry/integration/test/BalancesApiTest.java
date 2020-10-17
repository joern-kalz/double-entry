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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class BalancesApiTest {

    @Autowired
    TestSetup testSetup;

    MockMvc mockMvc;

    User loggedInUser;
    User otherUser;
    Account cashAccount;
    Account expenseAccount;
    Account foodAccount;
    Account foodAccountOfOtherUser;
    Account cashAccountOfOtherUser;

    @BeforeEach
    void setup() {
        testSetup.clearDatabase();
        loggedInUser = testSetup.createUser("LOGGED_IN_USER", "");
        otherUser = testSetup.createUser("OTHER_USER", "");

        mockMvc = testSetup.createAuthenticatedMockMvc(loggedInUser);

        cashAccount = testSetup.createAccount("cash", loggedInUser);
        foodAccount = testSetup.createAccount("food", loggedInUser);
        expenseAccount = testSetup.createAccount("expenses", loggedInUser);
        testSetup.createParentChildLink(expenseAccount, foodAccount);
        foodAccountOfOtherUser = testSetup.createAccount("food of other user", otherUser);
        cashAccountOfOtherUser = testSetup.createAccount("cash of other user", otherUser);
    }

    @Test
    void shouldGetBalances() throws Exception {
        createTransaction(foodAccount, "1.99", cashAccount, "-1.99");
        createTransaction(foodAccount, "2.49", cashAccount, "-2.49");

        mockMvc.perform(get("/api/balances"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.accountId == " + foodAccount.getId() + " && @.balance == 4.48)]")
                        .exists())
                .andExpect(jsonPath("$[?(@.accountId == " + cashAccount.getId() + " && @.balance == -4.48)]")
                        .exists());
    }

    @Test
    void shouldGetBalancesForDateInterval() throws Exception {
        createTransactionForDate(LocalDate.of(2019, 1, 1),
                foodAccount, "1.89", cashAccount, "-1.89");
        createTransactionForDate(LocalDate.of(2020, 1, 1),
                foodAccount, "1.99", cashAccount, "-1.99");
        createTransactionForDate(LocalDate.of(2021, 1, 1),
                foodAccount, "2.49", cashAccount, "-2.49");

        mockMvc.perform(get("/api/balances?after=2020-01-01&before=2020-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.accountId == " + foodAccount.getId() + " && @.balance == 1.99)]")
                        .exists())
                .andExpect(jsonPath("$[?(@.accountId == " + cashAccount.getId() + " && @.balance == -1.99)]")
                        .exists());
    }

    @Test
    void shouldAddBalanceOfChildAccountToParentAccount() throws Exception {
        createTransaction(foodAccount, "1.99", cashAccount, "-1.99");

        mockMvc.perform(get("/api/balances"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.accountId == " + expenseAccount.getId() + " && @.balance == 1.99)]")
                        .exists())
                .andExpect(jsonPath("$[?(@.accountId == " + foodAccount.getId() + " && @.balance == 1.99)]")
                        .exists())
                .andExpect(jsonPath("$[?(@.accountId == " + cashAccount.getId() + " && @.balance == -1.99)]")
                        .exists());
    }

    @Test
    void shouldNotGetBalancesOfOtherUser() throws Exception {
        createTransactionForUser(loggedInUser,
                foodAccount, "1.89", cashAccount, "-1.89");
        createTransactionForUser(otherUser,
                foodAccountOfOtherUser, "2.49", cashAccountOfOtherUser, "-2.49");

        mockMvc.perform(get("/api/balances"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.accountId == " + foodAccount.getId() + " && @.balance == 1.89)]")
                        .exists())
                .andExpect(jsonPath("$[?(@.accountId == " + cashAccount.getId() + " && @.balance == -1.89)]")
                        .exists());
    }

    void createTransaction(Account debitAccount, String debitAmount,
                                   Account creditAccount, String creditAmount) {
        testSetup.createTransaction("", loggedInUser, LocalDate.of(2020, 1, 1),
                new TestTransactionEntry(debitAccount, debitAmount, false),
                new TestTransactionEntry(creditAccount, creditAmount, false));
    }

    void createTransactionForUser(User user, Account debitAccount, String debitAmount,
                                   Account creditAccount, String creditAmount) {
        testSetup.createTransaction("", user, LocalDate.of(2020, 1, 1),
                new TestTransactionEntry(debitAccount, debitAmount, false),
                new TestTransactionEntry(creditAccount, creditAmount, false));
    }

    void createTransactionForDate(LocalDate date, Account debitAccount, String debitAmount,
                                   Account creditAccount, String creditAmount) {
        testSetup.createTransaction("", loggedInUser, date,
                new TestTransactionEntry(debitAccount, debitAmount, false),
                new TestTransactionEntry(creditAccount, creditAmount, false));
    }
}
