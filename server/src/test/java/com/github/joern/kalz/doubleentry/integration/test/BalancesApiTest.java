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
public class BalancesApiTest {

    @Autowired
    private TestSetup testSetup;

    private MockMvc mockMvc;

    User loggedInUser;
    User otherUser;
    Account cashAccount;
    Account expenseAccount;
    Account foodAccount;
    Account foodAccountOfOtherUser;
    Account cashAccountOfOtherUser;

    @BeforeEach
    public void setup() {
        testSetup.clearDatabase();
        loggedInUser = testSetup.createUser("LOGGED_IN_USER", "");
        otherUser = testSetup.createUser("OTHER_USER", "");

        mockMvc = testSetup.createAuthenticatedMockMvc(loggedInUser);

        cashAccount = testSetup.createAccount("cash", loggedInUser, null);
        expenseAccount = testSetup.createAccount("expenses", loggedInUser, null);
        foodAccount = testSetup.createAccount("food", loggedInUser, expenseAccount);
        foodAccountOfOtherUser = testSetup.createAccount("food of other user", otherUser, null);
        cashAccountOfOtherUser = testSetup.createAccount("cash of other user", otherUser, null);
    }

    @Test
    public void shouldGetBalances() throws Exception {
        createTransaction(foodAccount, "1.99", cashAccount, "-1.99");
        createTransaction(foodAccount, "2.49", cashAccount, "-2.49");

        mockMvc.perform(get("/balances"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.accountId == " + foodAccount.getId() + " && @.balance == 4.48)]")
                        .exists())
                .andExpect(jsonPath("$[?(@.accountId == " + cashAccount.getId() + " && @.balance == -4.48)]")
                        .exists());
    }

    @Test
    public void shouldGetBalancesForDateInterval() throws Exception {
        createTransactionForDate(LocalDate.of(2019, 1, 1),
                foodAccount, "1.89", cashAccount, "-1.89");
        createTransactionForDate(LocalDate.of(2020, 1, 1),
                foodAccount, "1.99", cashAccount, "-1.99");
        createTransactionForDate(LocalDate.of(2021, 1, 1),
                foodAccount, "2.49", cashAccount, "-2.49");

        mockMvc.perform(get("/balances?after=2020-01-01&before=2020-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.accountId == " + foodAccount.getId() + " && @.balance == 1.99)]")
                        .exists())
                .andExpect(jsonPath("$[?(@.accountId == " + cashAccount.getId() + " && @.balance == -1.99)]")
                        .exists());
    }

    @Test
    public void shouldAddBalanceOfChildAccountToParentAccount() throws Exception {
        createTransaction(foodAccount, "1.99", cashAccount, "-1.99");

        mockMvc.perform(get("/balances"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.accountId == " + expenseAccount.getId() + " && @.balance == 1.99)]")
                        .exists())
                .andExpect(jsonPath("$[?(@.accountId == " + foodAccount.getId() + " && @.balance == 1.99)]")
                        .exists())
                .andExpect(jsonPath("$[?(@.accountId == " + cashAccount.getId() + " && @.balance == -1.99)]")
                        .exists());
    }

    @Test
    public void shouldNotGetBalancesOfOtherUser() throws Exception {
        createTransactionForUser(loggedInUser,
                foodAccount, "1.89", cashAccount, "-1.89");
        createTransactionForUser(otherUser,
                foodAccountOfOtherUser, "2.49", cashAccountOfOtherUser, "-2.49");

        mockMvc.perform(get("/balances"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.accountId == " + foodAccount.getId() + " && @.balance == 1.89)]")
                        .exists())
                .andExpect(jsonPath("$[?(@.accountId == " + cashAccount.getId() + " && @.balance == -1.89)]")
                        .exists());
    }

    private void createTransaction(Account debitAccount, String debitAmount,
                                   Account creditAccount, String creditAmount) {
        testSetup.createTransaction("", loggedInUser, LocalDate.of(2020, 1, 1),
                new TestTransactionEntry(debitAccount, debitAmount, false),
                new TestTransactionEntry(creditAccount, creditAmount, false));
    }

    private void createTransactionForUser(User user, Account debitAccount, String debitAmount,
                                   Account creditAccount, String creditAmount) {
        testSetup.createTransaction("", user, LocalDate.of(2020, 1, 1),
                new TestTransactionEntry(debitAccount, debitAmount, false),
                new TestTransactionEntry(creditAccount, creditAmount, false));
    }

    private void createTransactionForDate(LocalDate date, Account debitAccount, String debitAmount,
                                   Account creditAccount, String creditAmount) {
        testSetup.createTransaction("", loggedInUser, date,
                new TestTransactionEntry(debitAccount, debitAmount, false),
                new TestTransactionEntry(creditAccount, creditAmount, false));
    }
}
