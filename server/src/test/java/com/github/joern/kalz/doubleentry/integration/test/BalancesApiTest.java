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
    Account expenseAccountOfOtherUser;
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
        expenseAccountOfOtherUser = testSetup.createAccount("expense of other user", otherUser);
        cashAccountOfOtherUser = testSetup.createAccount("cash of other user", otherUser);
    }

    @Test
    void shouldGetAbsoluteBalances() throws Exception {
        createTransaction("2020-01-01", foodAccount, "1.99", cashAccount, "-1.99");
        createTransaction("2020-01-02", foodAccount, "2.49", cashAccount, "-2.49");
        createTransaction("2020-01-03", foodAccount, "5.29", cashAccount, "-5.29");

        mockMvc.perform(get("/api/balances/absolute?date=2020-01-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].balances[?(@.accountId == " + foodAccount.getId() +
                        " && @.amount == 4.48)]").exists())
                .andExpect(jsonPath("$[0].balances[?(@.accountId == " + cashAccount.getId() +
                        " && @.amount == -4.48)]").exists());
    }

    @Test
    void shouldGetAbsoluteBalancesWithSteps() throws Exception {
        createTransactionForDate("2019-01-02", foodAccount, "1.89", cashAccount, "-1.89");
        createTransactionForDate("2019-12-31", foodAccount, "1.99", cashAccount, "-1.99");
        createTransactionForDate("2021-01-01", foodAccount, "2.49", cashAccount, "-2.49");

        mockMvc.perform(get("/api/balances/absolute?date=2019-01-01&stepMonths=12&stepCount=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[?(@.date == '2019-01-01')].balances[?(@.accountId == " +
                        cashAccount.getId() + " && @.amount == '0')]").exists())
                .andExpect(jsonPath("$[?(@.date == '2020-01-01')].balances[?(@.accountId == " +
                        cashAccount.getId() + " && @.amount == '-3.88')]").exists())
                .andExpect(jsonPath("$[?(@.date == '2021-01-01')].balances[?(@.accountId == " +
                        cashAccount.getId() + " && @.amount == '-6.37')]").exists());
    }

    @Test
    void shouldAddAbsoluteBalanceOfChildAccountToParentAccount() throws Exception {
        createTransaction("2020-01-01", foodAccount, "1.99", cashAccount, "-1.99");

        mockMvc.perform(get("/api/balances/absolute?date=2020-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].balances[?(@.accountId == " + expenseAccount.getId() +
                        " && @.amount == 1.99)]").exists())
                .andExpect(jsonPath("$[0].balances[?(@.accountId == " + foodAccount.getId() +
                        " && @.amount == 1.99)]").exists())
                .andExpect(jsonPath("$[0].balances[?(@.accountId == " + cashAccount.getId() +
                        " && @.amount == -1.99)]").exists());
    }

    @Test
    void shouldNotGetAbsoluteBalancesOfOtherUser() throws Exception {
        createTransactionForUser("2019-01-01", loggedInUser, expenseAccount, "1.99",
                cashAccount, "-1.99");
        createTransactionForUser("2019-01-01", otherUser, expenseAccountOfOtherUser, "3.89",
                cashAccountOfOtherUser, "-3.89");

        mockMvc.perform(get("/api/balances/absolute?date=2020-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].balances.length()").value(2))
                .andExpect(jsonPath("$[0].balances[?(@.accountId == " + expenseAccount.getId() +
                        " && @.amount == 1.99)]").exists())
                .andExpect(jsonPath("$[0].balances[?(@.accountId == " + cashAccount.getId() +
                        " && @.amount == -1.99)]").exists());
    }

    @Test
    void shouldGetRelativeBalances() throws Exception {
        createTransactionForDate("2019-01-02", foodAccount, "1.89", cashAccount, "-1.89");
        createTransactionForDate("2019-12-31", foodAccount, "1.99", cashAccount, "-1.99");
        createTransactionForDate("2021-01-01", foodAccount, "2.49", cashAccount, "-2.49");

        mockMvc.perform(get("/api/balances/relative?start=2019-01-01&stepMonths=12&stepCount=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.start == '2019-01-01' && @.end == '2019-12-31')]" +
                        ".differences[?(@.accountId == " + cashAccount.getId() + " && @.amount == '-3.88')]").exists())
                .andExpect(jsonPath("$[?(@.start == '2020-01-01' && @.end == '2020-12-31')]" +
                        ".differences[?(@.accountId == " + cashAccount.getId() + " && @.amount == '0')]").exists());
    }

    @Test
    void shouldAddRelativeBalanceOfChildAccountToParentAccount() throws Exception {
        createTransaction("2020-01-01", foodAccount, "1", cashAccount, "-1");

        mockMvc.perform(get("/api/balances/relative?start=2020-01-01&stepMonths=1&stepCount=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[?(@.start == '2020-01-01' && @.end == '2020-01-31')]" +
                        ".differences[?(@.accountId == " + cashAccount.getId() + " && @.amount == '-1.00')]").exists())
                .andExpect(jsonPath("$[?(@.start == '2020-01-01' && @.end == '2020-01-31')]" +
                        ".differences[?(@.accountId == " + foodAccount.getId() + " && @.amount == '1.00')]").exists())
                .andExpect(jsonPath("$[?(@.start == '2020-01-01' && @.end == '2020-01-31')]" +
                        ".differences[?(@.accountId == " + expenseAccount.getId() + " && @.amount == '1.00')]").exists());
    }

    @Test
    void shouldNotGetRelativeBalancesOfOtherUser() throws Exception {
        createTransactionForUser("2020-01-01", loggedInUser, expenseAccount, "1.99",
                cashAccount, "-1.99");
        createTransactionForUser("2020-01-01", otherUser, expenseAccountOfOtherUser, "3.89",
                cashAccountOfOtherUser, "-3.89");

        mockMvc.perform(get("/api/balances/absolute?date=2020-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].balances.length()").value(2))
                .andExpect(jsonPath("$[0].balances[?(@.accountId == " + expenseAccount.getId() +
                        " && @.amount == 1.99)]").exists())
                .andExpect(jsonPath("$[0].balances[?(@.accountId == " + cashAccount.getId() +
                        " && @.amount == -1.99)]").exists());
    }

    void createTransaction(String date, Account debitAccount, String debitAmount,
                           Account creditAccount, String creditAmount) {
        testSetup.createTransaction("", loggedInUser, LocalDate.parse(date),
                new TestTransactionEntry(debitAccount, debitAmount, false),
                new TestTransactionEntry(creditAccount, creditAmount, false));
    }

    void createTransactionForUser(String date, User user, Account debitAccount, String debitAmount,
                                  Account creditAccount, String creditAmount) {
        testSetup.createTransaction("", user, LocalDate.parse(date),
                new TestTransactionEntry(debitAccount, debitAmount, false),
                new TestTransactionEntry(creditAccount, creditAmount, false));
    }

    void createTransactionForDate(String date, Account debitAccount, String debitAmount,
                                  Account creditAccount, String creditAmount) {
        testSetup.createTransaction("", loggedInUser, LocalDate.parse(date),
                new TestTransactionEntry(debitAccount, debitAmount, false),
                new TestTransactionEntry(creditAccount, creditAmount, false));
    }
}
