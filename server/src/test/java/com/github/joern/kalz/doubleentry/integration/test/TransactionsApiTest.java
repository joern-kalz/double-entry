package com.github.joern.kalz.doubleentry.integration.test;

import com.github.joern.kalz.doubleentry.integration.test.setup.TestSetup;
import com.github.joern.kalz.doubleentry.models.Account;
import com.github.joern.kalz.doubleentry.models.Transaction;
import com.github.joern.kalz.doubleentry.models.TransactionsRepository;
import com.github.joern.kalz.doubleentry.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

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
    private Account carAccount;
    private Account cashAccount;
    private Account accountOfOtherUser;

    @BeforeEach
    public void setup() {
        testSetup.clearDatabase();
        loggedInUser = testSetup.createUser("LOGGED_IN_USERNAME", "");
        otherUser = testSetup.createUser("OTHER_USERNAME", "");

        mockMvc = testSetup.createAuthenticatedMockMvc(loggedInUser);

        foodAccount = testSetup.createAccount("food", loggedInUser, null);
        carAccount = testSetup.createAccount("car", loggedInUser, null);
        cashAccount = testSetup.createAccount("cash", loggedInUser, null);
        accountOfOtherUser = testSetup.createAccount("account of other user", otherUser, null);
    }

    @Test
    @Transactional
    public void shouldCreateTransaction() throws Exception {
        String requestBody = "{\"name\":\"bread and butter\",\"date\":\"2020-01-01\",\"entries\":[" +
                "{\"accountId\":" + cashAccount.getId() + ",\"amount\":-9.99}," +
                "{\"accountId\":" + foodAccount.getId() + ",\"amount\":9.99}]}";

        mockMvc.perform(post("/transactions").content(requestBody))
                .andExpect(status().isCreated());

        List<Transaction> transactions = new ArrayList<>();
        transactionsRepository.findAll().forEach(transactions::add);
        assertEquals(1, transactions.size());

        Transaction transaction = transactions.get(0);
        assertEquals(LocalDate.of(2020, 1, 1), transaction.getDate());
        assertEquals(2, transaction.getEntries().size());
    }

    @Test
    public void shouldNotCreateTransactionWithoutEntries() throws Exception {
        String requestBody = "{\"name\":\"bread and butter\",\"date\":\"2020-01-01\",\"entries\":[]}";

        mockMvc.perform(post("/transactions").content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldNotCreateTransactionWithDuplicatedAccount() throws Exception {
        String requestBody = "{\"name\":\"bread and butter\",\"date\":\"2020-01-01\",\"entries\":[" +
                "{\"accountId\":" + cashAccount.getId() + ",\"amount\":-9.99}," +
                "{\"accountId\":" + cashAccount.getId() + ",\"amount\":9.99}]}";

        mockMvc.perform(post("/transactions").content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldNotCreateTransactionWithNonZeroTotal() throws Exception {
        String requestBody = "{\"name\":\"bread and butter\",\"date\":\"2020-01-01\",\"entries\":[" +
                "{\"accountId\":" + cashAccount.getId() + ",\"amount\":-9.98}," +
                "{\"accountId\":" + foodAccount.getId() + ",\"amount\":9.99}]}";

        mockMvc.perform(post("/transactions").content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldNotCreateTransactionWithAccountOfOtherUser() throws Exception {
        String requestBody = "{\"name\":\"bread and butter\",\"date\":\"2020-01-01\",\"entries\":[" +
                "{\"accountId\":" + cashAccount.getId() + ",\"amount\":-9.99}," +
                "{\"accountId\":" + accountOfOtherUser.getId() + ",\"amount\":9.99}]}";

        mockMvc.perform(post("/transactions").content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldDeleteTransaction() throws Exception {
        long id = createTransactionWithUser("shopping", loggedInUser).getId();
        mockMvc.perform(delete("/transactions/" + id)).andExpect(status().isNoContent());
        assertTrue(transactionsRepository.findById(id).isEmpty());
    }

    @Test
    public void shouldNotDeleteTransactionOfOtherUser() throws Exception {
        long id = createTransactionWithUser("shopping", otherUser).getId();
        mockMvc.perform(delete("/transactions/" + id)).andExpect(status().isNotFound());
        assertFalse(transactionsRepository.findById(id).isEmpty());
    }

    @Test
    public void shouldGetTransaction() throws Exception {
        long id = createTransactionWithUser("supermarket", loggedInUser).getId();
        mockMvc.perform(get("/transactions/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("supermarket")));
    }

    @Test
    public void shouldNotGetTransactionOfOtherUser() throws Exception {
        long id = createTransactionWithUser("supermarket", otherUser).getId();
        mockMvc.perform(get("/transactions/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldGetTransactions() throws Exception {
        createTransactionWithUser("supermarket", loggedInUser);
        mockMvc.perform(get("/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is("supermarket")));
    }

    @Test
    public void shouldGetTransactionsByDate() throws Exception {
        createTransactionWithDate("first", LocalDate.of(2020, 1, 1));
        createTransactionWithDate("second", LocalDate.of(2020, 1, 2));
        createTransactionWithDate("third", LocalDate.of(2020, 1, 3));

        mockMvc.perform(get("/transactions?after=2020-01-02&before=2020-01-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].name", is("second")));
    }

    @Test
    public void shouldNotGetTransactionsIfQueryInvalid() throws Exception {
        mockMvc.perform(get("/transactions?after=2020-99-99"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldGetTransactionsByAccount() throws Exception {
        createTransactionWithAccounts("food", foodAccount, cashAccount);
        createTransactionWithAccounts("car", carAccount, cashAccount);

        mockMvc.perform(get("/transactions?accountId=" + carAccount.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].name", is("car")));
    }

    @Test
    public void shouldNotGetTransactionsOfOtherUser() throws Exception {
        createTransactionWithUser("supermarket", otherUser);
        mockMvc.perform(get("/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(0)));
    }

    @Test
    @Transactional
    public void shouldUpdateTransaction() throws Exception {
        long id = createTransactionWithUser("supermarket", loggedInUser).getId();
        String requestBody = "{\"name\":\"bread and butter\",\"date\":\"2020-01-01\",\"entries\":[" +
                "{\"accountId\":" + cashAccount.getId() + ",\"amount\":-99.99}," +
                "{\"accountId\":" + foodAccount.getId() + ",\"amount\":99.99}]}";

        mockMvc.perform(put("/transactions/" + id).content(requestBody))
                .andExpect(status().isNoContent());

        Optional<Transaction> transaction = transactionsRepository.findById(id);
        assertTrue(transaction.isPresent());
        assertEquals("bread and butter", transaction.get().getName());
        assertEquals(2, transaction.get().getEntries().size());
        BigDecimal absoluteEntryAmount = transaction.get().getEntries().get(0).getAmount().abs();
        assertEquals(0, new BigDecimal("99.99").compareTo(absoluteEntryAmount));
    }

    @Test
    @Transactional
    public void shouldNotUpdateTransactionOfOtherUser() throws Exception {
        long id = createTransactionWithUser("supermarket", otherUser).getId();
        String requestBody = "{\"name\":\"bread and butter\",\"date\":\"2020-01-01\",\"entries\":[" +
                "{\"accountId\":" + cashAccount.getId() + ",\"amount\":-99.99}," +
                "{\"accountId\":" + foodAccount.getId() + ",\"amount\":99.99}]}";

        mockMvc.perform(put("/transactions/" + id).content(requestBody))
                .andExpect(status().isNotFound());
    }

    private Transaction createTransactionWithUser(String name, User user) {
        return testSetup.createTransaction(name, user, LocalDate.of(2020, 1, 1),
                foodAccount, "9.99", cashAccount, "-9.99");
    }

    private Transaction createTransactionWithDate(String name, LocalDate date) {
        return testSetup.createTransaction(name, loggedInUser, date,
                foodAccount, "9.99", cashAccount, "-9.99");
    }

    private Transaction createTransactionWithAccounts(String name, Account debitAccount, Account creditAccount) {
        return testSetup.createTransaction(name, loggedInUser, LocalDate.of(2020, 1, 1),
                debitAccount, "9.99", creditAccount, "-9.99");
    }
}
