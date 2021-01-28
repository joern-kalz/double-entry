package com.github.joern.kalz.doubleentry.integration.test;

import com.github.joern.kalz.doubleentry.integration.test.setup.TestSetup;
import com.github.joern.kalz.doubleentry.integration.test.setup.TestTransactionEntry;
import com.github.joern.kalz.doubleentry.models.Account;
import com.github.joern.kalz.doubleentry.models.TransactionsRepository;
import com.github.joern.kalz.doubleentry.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class PortfolioReturnsApiTest {
    MockMvc mockMvc;

    @Autowired
    TestSetup testSetup;

    @Autowired
    TransactionsRepository transactionsRepository;

    User loggedInUser;
    User otherUser;
    Account assetAccount;
    Account revenueAccount;
    Account expenseAccount;
    Account portfolioAccount;
    Account portfolioChildAccount;
    Account revenueAccountOfOtherUser;
    Account expenseAccountOfOtherUser;
    Account portfolioAccountOfOtherUser;

    @BeforeEach
    void setup() {
        testSetup.clearDatabase();
        loggedInUser = testSetup.createUser("LOGGED_IN_USERNAME", "");
        otherUser = testSetup.createUser("OTHER_USERNAME", "");

        mockMvc = testSetup.createAuthenticatedMockMvc(loggedInUser);

        assetAccount = testSetup.createAccount("asset", loggedInUser);
        revenueAccount = testSetup.createAccount("revenue", loggedInUser);
        expenseAccount = testSetup.createAccount("expense", loggedInUser);
        portfolioAccount = testSetup.createAccount("portfolio", loggedInUser);
        portfolioChildAccount = testSetup.createAccount("portfolioChild", loggedInUser);
        revenueAccountOfOtherUser = testSetup.createAccount("revenue account of other user", otherUser);
        expenseAccountOfOtherUser = testSetup.createAccount("expense account of other user", otherUser);
        portfolioAccountOfOtherUser = testSetup.createAccount("portfolio account of other user", otherUser);

        testSetup.createParentChildLink(assetAccount, portfolioAccount);
        testSetup.createParentChildLink(portfolioAccount, portfolioChildAccount);
    }

    @Test
    void shouldCalculatePerformance() throws Exception {
        createTransaction("2020-02-01", "110.00", assetAccount, portfolioAccount);
        createTransaction("2020-03-01", "100.00", assetAccount, portfolioAccount);
        createTransaction("2020-10-01", "4.00", revenueAccount, portfolioAccount);
        createTransaction("2020-11-01", "4.31", revenueAccount, portfolioAccount);

        mockMvc.perform(get("/api/portfolio-returns?portfolioAccountId=" + portfolioAccount.getId() +
                "&revenueAccountId=" + revenueAccount.getId() + "&expenseAccountId=" + expenseAccount.getId() +
                "&until=2020-12-01&stepYears=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].start", is("2020-02-01")))
                .andExpect(jsonPath("$[0].end", is("2020-12-01")))
                .andExpect(jsonPath("$[0].portfolioReturn", is(5.0)));
    }

    @Test
    void shouldListMultipleYears() throws Exception {
        createTransaction("2019-11-01", "50.00", assetAccount, portfolioAccount);
        createTransaction("2019-12-31", "0.33", revenueAccount, portfolioAccount);
        createTransaction("2020-01-01", "170.00", assetAccount, portfolioAccount);
        createTransaction("2020-12-31", "20.00", portfolioAccount, assetAccount);
        createTransaction("2021-01-01", "2.50", portfolioAccount, revenueAccount);

        mockMvc.perform(get("/api/portfolio-returns?portfolioAccountId=" + portfolioAccount.getId() +
                "&revenueAccountId=" + revenueAccount.getId() + "&expenseAccountId=" + expenseAccount.getId() +
                "&until=2021-03-15&stepYears=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(3)))
                .andExpect(jsonPath("$[0].start", is("2019-11-01")))
                .andExpect(jsonPath("$[0].end", is("2019-12-31")))
                .andExpect(jsonPath("$[0].portfolioReturn", is(4.0)))
                .andExpect(jsonPath("$[1].start", is("2020-01-01")))
                .andExpect(jsonPath("$[1].end", is("2020-12-31")))
                .andExpect(jsonPath("$[1].portfolioReturn", is(0.0)))
                .andExpect(jsonPath("$[2].start", is("2021-01-01")))
                .andExpect(jsonPath("$[2].end", is("2021-03-15")))
                .andExpect(jsonPath("$[2].portfolioReturn", is(-6.0)));
    }

    @Test
    void shouldIncludeChildAccounts() throws Exception {
        createTransaction("2020-02-10", "40.00", assetAccount, portfolioAccount);
        createTransaction("2020-02-10", "80.00", assetAccount, portfolioChildAccount);
        createTransaction("2020-08-01", "5.71", revenueAccount, portfolioAccount);

        mockMvc.perform(get("/api/portfolio-returns?portfolioAccountId=" + portfolioAccount.getId() +
                "&revenueAccountId=" + revenueAccount.getId() + "&expenseAccountId=" + expenseAccount.getId() +
                "&until=2020-08-05&stepYears=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].start", is("2020-02-10")))
                .andExpect(jsonPath("$[0].end", is("2020-08-05")))
                .andExpect(jsonPath("$[0].portfolioReturn", is(10.0)));
    }

    @Test
    void shouldIgnoreNonPortfolioTransactions() throws Exception {
        createTransaction("2020-02-10", "40.00", assetAccount, portfolioAccount);
        createTransaction("2020-08-01", "5.71", revenueAccount, assetAccount);

        mockMvc.perform(get("/api/portfolio-returns?portfolioAccountId=" + portfolioAccount.getId() +
                "&revenueAccountId=" + revenueAccount.getId() + "&expenseAccountId=" + expenseAccount.getId() +
                "&until=2020-08-05&stepYears=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].start", is("2020-02-10")))
                .andExpect(jsonPath("$[0].end", is("2020-08-05")))
                .andExpect(jsonPath("$[0].portfolioReturn", is(0.0)));
    }

    @Test
    void shouldNotAcceptLeadingAdjustment() throws Exception {
        createTransaction("2007-08-01", "5.71", revenueAccount, portfolioAccount);
        createTransaction("2020-02-10", "40.00", assetAccount, portfolioAccount);

        mockMvc.perform(get("/api/portfolio-returns?portfolioAccountId=" + portfolioAccount.getId() +
                "&revenueAccountId=" + revenueAccount.getId() + "&expenseAccountId=" + expenseAccount.getId() +
                "&until=2020-08-05&stepYears=1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCalculateForAccountOfOtherUser() throws Exception {
        createTransaction("2020-02-10", "40.00", revenueAccountOfOtherUser, portfolioAccountOfOtherUser);

        mockMvc.perform(get("/api/portfolio-returns?" +
                "portfolioAccountId=" + portfolioAccountOfOtherUser.getId() +
                "&revenueAccountId=" + revenueAccountOfOtherUser.getId() +
                "&expenseAccountId=" + expenseAccountOfOtherUser.getId() +
                "&until=2020-08-05&stepYears=1"))
                .andExpect(status().isNotFound());
    }

    void createTransaction(String date, String amount, Account creditAccount, Account debitAccount) {
        String inverseAmount = new BigDecimal(amount).multiply(new BigDecimal("-1")).toString();

        testSetup.createTransaction("deposit", loggedInUser, LocalDate.parse(date),
                new TestTransactionEntry(creditAccount, inverseAmount, true),
                new TestTransactionEntry(debitAccount, amount, true));
    }
}
