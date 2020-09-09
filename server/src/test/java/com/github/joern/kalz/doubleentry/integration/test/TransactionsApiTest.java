package com.github.joern.kalz.doubleentry.integration.test;

import com.github.joern.kalz.doubleentry.integration.test.setup.TestSetup;
import com.github.joern.kalz.doubleentry.integration.test.setup.TestTransactionEntry;
import com.github.joern.kalz.doubleentry.models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class TransactionsApiTest {

    private MockMvc mockMvc;

    @Autowired
    private TransactionsRepository transactionsRepository;

    @Autowired
    private TestSetup testSetup;

    private User loggedInUser;
    private User otherUser;
    private Account foodAccount;
    private Account expenseAccount;
    private Account cashAccount;
    private Account accountOfOtherUser;

    @BeforeEach
    public void setup() {
        testSetup.clearDatabase();
        loggedInUser = testSetup.createUser("LOGGED_IN_USERNAME", "");
        otherUser = testSetup.createUser("OTHER_USERNAME", "");

        mockMvc = testSetup.createAuthenticatedMockMvc(loggedInUser);

        foodAccount = testSetup.createAccount("food", loggedInUser);
        expenseAccount = testSetup.createAccount("expense", loggedInUser);
        testSetup.createParentChildLink(expenseAccount, foodAccount);
        cashAccount = testSetup.createAccount("cash", loggedInUser);
        accountOfOtherUser = testSetup.createAccount("account of other user", otherUser);
    }

    @Test
    public void shouldCreateTransaction() throws Exception {
        String requestBody = "{\"name\":\"bread and butter\",\"date\":\"2020-01-01\",\"entries\":[" +
                "{\"accountId\":" + cashAccount.getId() + ",\"amount\":-9.99}," +
                "{\"accountId\":" + foodAccount.getId() + ",\"amount\":9.99}]}";

        mockMvc.perform(post("/api/transactions").content(requestBody))
                .andExpect(status().isCreated());

        List<Transaction> transactions = new ArrayList<>(transactionsRepository.findAll());
        assertEquals(1, transactions.size());

        Transaction transaction = transactions.get(0);
        assertEquals(LocalDate.of(2020, 1, 1), transaction.getDate());
        assertEquals(2, transaction.getEntries().size());
    }

    @Test
    public void shouldNotCreateTransactionWithoutEntries() throws Exception {
        String requestBody = "{\"name\":\"bread and butter\",\"date\":\"2020-01-01\",\"entries\":[]}";

        mockMvc.perform(post("/api/transactions").content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldNotCreateTransactionWithDuplicatedAccount() throws Exception {
        String requestBody = "{\"name\":\"bread and butter\",\"date\":\"2020-01-01\",\"entries\":[" +
                "{\"accountId\":" + cashAccount.getId() + ",\"amount\":-9.99}," +
                "{\"accountId\":" + cashAccount.getId() + ",\"amount\":9.99}]}";

        mockMvc.perform(post("/api/transactions").content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldNotCreateTransactionWithNonZeroTotal() throws Exception {
        String requestBody = "{\"name\":\"bread and butter\",\"date\":\"2020-01-01\",\"entries\":[" +
                "{\"accountId\":" + cashAccount.getId() + ",\"amount\":-9.98}," +
                "{\"accountId\":" + foodAccount.getId() + ",\"amount\":9.99}]}";

        mockMvc.perform(post("/api/transactions").content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldNotCreateTransactionWithAccountOfOtherUser() throws Exception {
        String requestBody = "{\"name\":\"bread and butter\",\"date\":\"2020-01-01\",\"entries\":[" +
                "{\"accountId\":" + cashAccount.getId() + ",\"amount\":-9.99}," +
                "{\"accountId\":" + accountOfOtherUser.getId() + ",\"amount\":9.99}]}";

        mockMvc.perform(post("/api/transactions").content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldDeleteTransaction() throws Exception {
        long id = createTransactionWithUser("shopping", loggedInUser).getId();
        mockMvc.perform(delete("/api/transactions/" + id)).andExpect(status().isNoContent());
        assertTrue(transactionsRepository.findById(id).isEmpty());
    }

    @Test
    public void shouldNotDeleteTransactionOfOtherUser() throws Exception {
        long id = createTransactionWithUser("shopping", otherUser).getId();
        mockMvc.perform(delete("/api/transactions/" + id)).andExpect(status().isNotFound());
        assertFalse(transactionsRepository.findById(id).isEmpty());
    }

    @Test
    public void shouldGetTransaction() throws Exception {
        long id = createTransactionWithUser("supermarket", loggedInUser).getId();
        mockMvc.perform(get("/api/transactions/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("supermarket")));
    }

    @Test
    public void shouldNotGetTransactionOfOtherUser() throws Exception {
        long id = createTransactionWithUser("supermarket", otherUser).getId();
        mockMvc.perform(get("/api/transactions/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldGetTransactions() throws Exception {
        createTransactionWithUser("supermarket", loggedInUser);
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is("supermarket")));
    }

    @Test
    public void shouldGetTransactionsByDate() throws Exception {
        createTransactionWithDate("first", LocalDate.of(2020, 1, 1));
        createTransactionWithDate("second", LocalDate.of(2020, 1, 2));
        createTransactionWithDate("third", LocalDate.of(2020, 1, 3));

        mockMvc.perform(get("/api/transactions?after=2020-01-02&before=2020-01-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].name", is("second")));
    }

    @Test
    public void shouldNotGetTransactionsIfQueryInvalid() throws Exception {
        mockMvc.perform(get("/api/transactions?after=2020-99-99"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldGetTransactionsByAccount() throws Exception {
        createTransactionWithAccounts("food", foodAccount, cashAccount);
        createTransactionWithAccounts("expense", expenseAccount, cashAccount);

        mockMvc.perform(get("/api/transactions?accountId=" + foodAccount.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].name", is("food")));
    }

    @Test
    public void shouldGetTransactionsOfChildAccounts() throws Exception {
        createTransactionWithAccounts("food", foodAccount, cashAccount);
        createTransactionWithAccounts("expense", expenseAccount, cashAccount);

        mockMvc.perform(get("/api/transactions?accountId=" + expenseAccount.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[?(@.name == 'expense')]").exists())
                .andExpect(jsonPath("$[?(@.name == 'food')]").exists());
    }

    @Test
    public void shouldNotGetTransactionsOfOtherUser() throws Exception {
        createTransactionWithUser("supermarket", otherUser);
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(0)));
    }

    @Test
    public void shouldUpdateTransaction() throws Exception {
        long id = createTransactionWithUser("supermarket", loggedInUser).getId();
        String requestBody = "{\"name\":\"bread and butter\",\"date\":\"2020-01-01\",\"entries\":[" +
                "{\"accountId\":" + cashAccount.getId() + ",\"amount\":-99.99}," +
                "{\"accountId\":" + expenseAccount.getId() + ",\"amount\":99.99}]}";

        mockMvc.perform(put("/api/transactions/" + id).content(requestBody))
                .andExpect(status().isNoContent());

        Optional<Transaction> transactionOptional = transactionsRepository.findById(id);
        assertTrue(transactionOptional.isPresent());

        Transaction transaction = transactionOptional.get();
        assertEquals("bread and butter", transaction.getName());
        assertEquals(2, transaction.getEntries().size());

        Optional<Entry> expenseEntry = transaction.getEntries().stream()
                .filter(entry -> entry.getId().getAccount().getName().equals(expenseAccount.getName()))
                .findAny();
        assertTrue(expenseEntry.isPresent());
        assertEquals(0, new BigDecimal("99.99").compareTo(expenseEntry.get().getAmount()));
    }

    @Test
    public void shouldNotUpdateTransactionOfOtherUser() throws Exception {
        long id = createTransactionWithUser("supermarket", otherUser).getId();
        String requestBody = "{\"name\":\"bread and butter\",\"date\":\"2020-01-01\",\"entries\":[" +
                "{\"accountId\":" + cashAccount.getId() + ",\"amount\":-99.99}," +
                "{\"accountId\":" + foodAccount.getId() + ",\"amount\":99.99}]}";

        mockMvc.perform(put("/api/transactions/" + id).content(requestBody))
                .andExpect(status().isNotFound());
    }

    private Transaction createTransactionWithUser(String name, User user) {
        return testSetup.createTransaction(name, user, LocalDate.of(2020, 1, 1),
                new TestTransactionEntry(foodAccount, "9.99", false),
                new TestTransactionEntry(cashAccount,  "-9.99", false));
    }

    private void createTransactionWithDate(String name, LocalDate date) {
        testSetup.createTransaction(name, loggedInUser, date,
                new TestTransactionEntry(foodAccount, "9.99", false),
                new TestTransactionEntry(cashAccount,  "-9.99", false));
    }

    private void createTransactionWithAccounts(String name, Account debitAccount, Account creditAccount) {
        testSetup.createTransaction(name, loggedInUser, LocalDate.of(2020, 1, 1),
                new TestTransactionEntry(debitAccount, "9.99", false),
                new TestTransactionEntry(creditAccount,  "-9.99", false));
    }
}
