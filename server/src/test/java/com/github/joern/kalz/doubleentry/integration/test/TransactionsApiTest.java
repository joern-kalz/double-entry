package com.github.joern.kalz.doubleentry.integration.test;

import com.github.joern.kalz.doubleentry.models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class TransactionsApiTest {

    private static final String LOGGED_IN_USERNAME = "LOGGED_IN_USER";
    private static final String OTHER_USERNAME = "OTHER_USER";

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private AccountsRepository accountsRepository;

    @Autowired
    private TransactionsRepository transactionsRepository;

    @Autowired
    private UsersRepository usersRepository;

    private User loggedInUser;
    private User otherUser;
    private Account foodAccount;
    private Account carAccount;
    private Account cashAccount;
    private Account accountOfOtherUser;

    @BeforeEach
    public void setup() {
        transactionsRepository.deleteAll();
        usersRepository.deleteAll();
        loggedInUser = usersRepository.save(new User(LOGGED_IN_USERNAME, null, true));
        otherUser = usersRepository.save(new User(OTHER_USERNAME, null, true));

        foodAccount = accountsRepository.save(createAccount("food", loggedInUser));
        carAccount = accountsRepository.save(createAccount("car", loggedInUser));
        cashAccount = accountsRepository.save(createAccount("cash", loggedInUser));
        accountOfOtherUser = accountsRepository.save(createAccount("account of other user", otherUser));

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .defaultRequest(get("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(LOGGED_IN_USERNAME)).with(csrf()))
                .alwaysDo(MockMvcResultHandlers.print())
                .build();
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
        long id = transactionsRepository.save(createTransactionWithUser("shopping", loggedInUser)).getId();
        mockMvc.perform(delete("/transactions/" + id)).andExpect(status().isNoContent());
        assertTrue(transactionsRepository.findById(id).isEmpty());
    }

    @Test
    public void shouldNotDeleteTransactionOfOtherUser() throws Exception {
        long id = transactionsRepository.save(createTransactionWithUser("shopping", otherUser)).getId();
        mockMvc.perform(delete("/transactions/" + id)).andExpect(status().isNotFound());
        assertFalse(transactionsRepository.findById(id).isEmpty());
    }

    @Test
    public void shouldGetTransaction() throws Exception {
        long id = transactionsRepository.save(createTransactionWithUser("supermarket", loggedInUser)).getId();
        mockMvc.perform(get("/transactions/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("supermarket")));
    }

    @Test
    public void shouldNotGetTransactionOfOtherUser() throws Exception {
        long id = transactionsRepository.save(createTransactionWithUser("supermarket", otherUser)).getId();
        mockMvc.perform(get("/transactions/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldGetTransactions() throws Exception {
        transactionsRepository.save(createTransactionWithUser("supermarket", loggedInUser));
        mockMvc.perform(get("/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is("supermarket")));
    }

    @Test
    public void shouldGetTransactionsByDate() throws Exception {
        transactionsRepository.save(createTransactionWithDate("first",
                LocalDate.of(2020, 1, 1)));
        transactionsRepository.save(createTransactionWithDate("second",
                LocalDate.of(2020, 1, 2)));
        transactionsRepository.save(createTransactionWithDate("third",
                LocalDate.of(2020, 1, 3)));

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
        transactionsRepository.save(createTransactionWithAccounts("food", foodAccount, cashAccount));
        transactionsRepository.save(createTransactionWithAccounts("car", carAccount, cashAccount));

        mockMvc.perform(get("/transactions?accountId=" + carAccount.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].name", is("car")));
    }

    @Test
    public void shouldNotGetTransactionsOfOtherUser() throws Exception {
        transactionsRepository.save(createTransactionWithUser("supermarket", otherUser));
        mockMvc.perform(get("/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(0)));
    }

    @Test
    @Transactional
    public void shouldUpdateTransaction() throws Exception {
        long id = transactionsRepository.save(createTransactionWithUser("supermarket", loggedInUser)).getId();
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
        long id = transactionsRepository.save(createTransactionWithUser("supermarket", otherUser)).getId();
        String requestBody = "{\"name\":\"bread and butter\",\"date\":\"2020-01-01\",\"entries\":[" +
                "{\"accountId\":" + cashAccount.getId() + ",\"amount\":-99.99}," +
                "{\"accountId\":" + foodAccount.getId() + ",\"amount\":99.99}]}";

        mockMvc.perform(put("/transactions/" + id).content(requestBody))
                .andExpect(status().isNotFound());
    }

    private Account createAccount(String name, User user) {
        Account account = new Account();
        account.setUser(user);
        account.setName(name);
        return account;
    }

    private Transaction createTransactionWithUser(String name, User user) {
        return createTransaction(name, user, LocalDate.of(2020, 1, 1), foodAccount, cashAccount);
    }

    private Transaction createTransactionWithDate(String name, LocalDate date) {
        return createTransaction(name, loggedInUser, date, foodAccount, cashAccount);
    }

    private Transaction createTransactionWithAccounts(String name, Account debitAccount, Account creditAccount) {
        return createTransaction(name, loggedInUser, LocalDate.of(2020, 1, 1), debitAccount,
                creditAccount);
    }

    private Transaction createTransaction(String name, User user, LocalDate date, Account debitAccount,
                                          Account creditAccount) {
        Transaction transaction = new Transaction();
        transaction.setName(name);
        transaction.setDate(date);
        transaction.setUser(user);

        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(transaction, debitAccount, new BigDecimal("9.99"), false));
        entries.add(new Entry(transaction, creditAccount, new BigDecimal("-9.99"), false));
        transaction.setEntries(entries);

        return transaction;
    }
}
