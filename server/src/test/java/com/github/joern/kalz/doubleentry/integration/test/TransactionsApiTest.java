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
import java.util.*;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class TransactionsApiTest {

    MockMvc mockMvc;

    @Autowired
    TransactionsRepository transactionsRepository;

    @Autowired
    TestSetup testSetup;

    User loggedInUser;
    User otherUser;
    Account foodAccount;
    Account clothingAccount;
    Account expenseAccount;
    Account cashAccount;
    Account accountOfOtherUser;

    @BeforeEach
    void setup() {
        testSetup.clearDatabase();
        loggedInUser = testSetup.createUser("LOGGED_IN_USERNAME", "");
        otherUser = testSetup.createUser("OTHER_USERNAME", "");

        mockMvc = testSetup.createAuthenticatedMockMvc(loggedInUser);

        foodAccount = testSetup.createAccount("food", loggedInUser);
        clothingAccount = testSetup.createAccount("clothing", loggedInUser);
        expenseAccount = testSetup.createAccount("expense", loggedInUser);
        testSetup.createParentChildLink(expenseAccount, foodAccount);
        testSetup.createParentChildLink(expenseAccount, clothingAccount);
        cashAccount = testSetup.createAccount("cash", loggedInUser);
        accountOfOtherUser = testSetup.createAccount("account of other user", otherUser);
    }

    @Test
    void shouldCreateTransaction() throws Exception {
        String requestBody = "{\"name\":\"bread and butter\",\"date\":\"2020-01-01\",\"entries\":[" +
                "{\"accountId\":" + cashAccount.getId() + ",\"amount\":\"-9.99\"}," +
                "{\"accountId\":" + foodAccount.getId() + ",\"amount\":\"9.99\"}]}";

        mockMvc.perform(post("/api/transactions").content(requestBody))
                .andExpect(status().isCreated());

        List<Transaction> transactions = new ArrayList<>(transactionsRepository.findAll());
        assertEquals(1, transactions.size());

        Transaction transaction = transactions.get(0);
        assertEquals(LocalDate.of(2020, 1, 1), transaction.getDate());
        assertEquals(2, transaction.getEntries().size());
    }

    @Test
    void shouldNotCreateTransactionWithoutEntries() throws Exception {
        String requestBody = "{\"name\":\"bread and butter\",\"date\":\"2020-01-01\",\"entries\":[]}";

        mockMvc.perform(post("/api/transactions").content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotCreateTransactionWithDuplicatedAccount() throws Exception {
        String requestBody = "{\"name\":\"bread and butter\",\"date\":\"2020-01-01\",\"entries\":[" +
                "{\"accountId\":" + cashAccount.getId() + ",\"amount\":\"-9.99\"}," +
                "{\"accountId\":" + cashAccount.getId() + ",\"amount\":\"9.99\"}]}";

        mockMvc.perform(post("/api/transactions").content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotCreateTransactionWithNonZeroTotal() throws Exception {
        String requestBody = "{\"name\":\"bread and butter\",\"date\":\"2020-01-01\",\"entries\":[" +
                "{\"accountId\":" + cashAccount.getId() + ",\"amount\":\"-9.98\"}," +
                "{\"accountId\":" + foodAccount.getId() + ",\"amount\":\"9.99\"}]}";

        mockMvc.perform(post("/api/transactions").content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotCreateTransactionWithAccountOfOtherUser() throws Exception {
        String requestBody = "{\"name\":\"bread and butter\",\"date\":\"2020-01-01\",\"entries\":[" +
                "{\"accountId\":" + cashAccount.getId() + ",\"amount\":\"-9.99\"}," +
                "{\"accountId\":" + accountOfOtherUser.getId() + ",\"amount\":\"9.99\"}]}";

        mockMvc.perform(post("/api/transactions").content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldDeleteTransaction() throws Exception {
        long id = createTransactionWithUser("shopping", loggedInUser).getId();
        mockMvc.perform(delete("/api/transactions/" + id)).andExpect(status().isNoContent());
        assertTrue(transactionsRepository.findById(id).isEmpty());
    }

    @Test
    void shouldNotDeleteTransactionOfOtherUser() throws Exception {
        long id = createTransactionWithUser("shopping", otherUser).getId();
        mockMvc.perform(delete("/api/transactions/" + id)).andExpect(status().isNotFound());
        assertFalse(transactionsRepository.findById(id).isEmpty());
    }

    @Test
    void shouldGetTransaction() throws Exception {
        long id = createTransactionWithAmount("supermarket", "12.34").getId();
        mockMvc.perform(get("/api/transactions/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("supermarket")))
                .andExpect(jsonPath("$.entries.length()", is(2)))
                .andExpect(jsonPath("$.entries[?(@.amount == '12.34')]").exists())
                .andExpect(jsonPath("$.entries[?(@.amount == '-12.34')]").exists());
    }

    @Test
    void shouldNotGetTransactionOfOtherUser() throws Exception {
        long id = createTransactionWithUser("supermarket", otherUser).getId();
        mockMvc.perform(get("/api/transactions/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetTransactions() throws Exception {
        createTransactionWithAmount("grocery", "1.34");
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is("grocery")))
                .andExpect(jsonPath("$[0].entries.length()", is(2)))
                .andExpect(jsonPath("$[0].entries[?(@.amount == '1.34')]").exists())
                .andExpect(jsonPath("$[0].entries[?(@.amount == '-1.34')]").exists());
    }

    @Test
    void shouldGetTransactionsByDate() throws Exception {
        createTransactionWithDate("first", LocalDate.of(2020, 1, 1));
        createTransactionWithDate("second", LocalDate.of(2020, 1, 2));
        createTransactionWithDate("third", LocalDate.of(2020, 1, 3));

        mockMvc.perform(get("/api/transactions?after=2020-01-02&before=2020-01-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].name", is("second")));
    }

    @Test
    void shouldNotGetTransactionsIfQueryInvalid() throws Exception {
        mockMvc.perform(get("/api/transactions?after=2020-99-99"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetTransactionsByAccount() throws Exception {
        createTransactionWithAccounts("food", foodAccount, cashAccount);
        createTransactionWithAccounts("expense", expenseAccount, cashAccount);

        mockMvc.perform(get("/api/transactions?accountId=" + foodAccount.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].name", is("food")));
    }

    @Test
    void shouldGetTransactionsOfChildAccounts() throws Exception {
        createTransactionWithAccounts("food", foodAccount, cashAccount);
        createTransactionWithAccounts("expense", expenseAccount, cashAccount);

        mockMvc.perform(get("/api/transactions?accountId=" + expenseAccount.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[?(@.name == 'expense')]").exists())
                .andExpect(jsonPath("$[?(@.name == 'food')]").exists());
    }

    @Test
    void shouldGetTransactionsByDebitAccount() throws Exception {
        createTransactionWithAccounts("food and clothing", Arrays.asList(foodAccount, clothingAccount),
                Collections.singletonList(cashAccount));
        createTransactionWithAccounts("expense", Collections.singletonList(expenseAccount),
                Collections.singletonList(cashAccount));
        createTransactionWithAccounts("expense-cancellation", cashAccount, expenseAccount);

        mockMvc.perform(get("/api/transactions?debitAccountId=" + expenseAccount.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[?(@.name == 'food and clothing')]").exists())
                .andExpect(jsonPath("$[?(@.name == 'food and clothing')].entries.length()",
                        is(Collections.singletonList(3))))
                .andExpect(jsonPath("$[?(@.name == 'expense')]").exists());
    }

    @Test
    void shouldGetTransactionsByCreditAccount() throws Exception {
        createTransactionWithAccounts("food-cancellation", cashAccount, foodAccount);
        createTransactionWithAccounts("expense-cancellation", cashAccount, expenseAccount);
        createTransactionWithAccounts("expense", expenseAccount, cashAccount);

        mockMvc.perform(get("/api/transactions?creditAccountId=" + expenseAccount.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[?(@.name == 'food-cancellation')]").exists())
                .andExpect(jsonPath("$[?(@.name == 'expense-cancellation')]").exists());
    }

    @Test
    void shouldGetTransactionsByName() throws Exception {
        createTransaction("weekend shopping");
        createTransaction("grocery");

        mockMvc.perform(get("/api/transactions?name=*shop*"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].name", is("weekend shopping")));
    }

    @Test
    void shouldGetTransactionsByPage() throws Exception {
        createTransactionWithDate("first", LocalDate.of(2020, 1, 1));
        createTransactionWithDate("second", LocalDate.of(2020, 1, 2));
        createTransactionWithDate("third", LocalDate.of(2020, 1, 3));

        mockMvc.perform(get("/api/transactions?page=1&size=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].name", is("second")));
    }

    @Test
    void shouldNotGetTransactionsOfOtherUser() throws Exception {
        createTransactionWithUser("supermarket", otherUser);
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(0)));
    }

    @Test
    void shouldGetTransactionsSortedAscending() throws Exception {
        createTransactionWithDate("first", LocalDate.of(2020, 1, 1));
        createTransactionWithDate("second", LocalDate.of(2021, 1, 1));

        mockMvc.perform(get("/api/transactions?sort=dateAscending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].date", is("2020-01-01")))
                .andExpect(jsonPath("$[1].date", is("2021-01-01")));
    }

    @Test
    void shouldGetTransactionsSortedDescending() throws Exception {
        createTransactionWithDate("first", LocalDate.of(2020, 1, 1));
        createTransactionWithDate("second", LocalDate.of(2021, 1, 1));

        mockMvc.perform(get("/api/transactions?sort=dateDescending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].date", is("2021-01-01")))
                .andExpect(jsonPath("$[1].date", is("2020-01-01")));
    }

    @Test
    @Transactional
    void shouldUpdateTransaction() throws Exception {
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
    void shouldNotUpdateTransactionOfOtherUser() throws Exception {
        long id = createTransactionWithUser("supermarket", otherUser).getId();
        String requestBody = "{\"name\":\"bread and butter\",\"date\":\"2020-01-01\",\"entries\":[" +
                "{\"accountId\":" + cashAccount.getId() + ",\"amount\":-99.99}," +
                "{\"accountId\":" + foodAccount.getId() + ",\"amount\":99.99}]}";

        mockMvc.perform(put("/api/transactions/" + id).content(requestBody))
                .andExpect(status().isNotFound());
    }

    void createTransaction(String name) {
        testSetup.createTransaction(name, loggedInUser, LocalDate.of(2020, 1, 1),
                new TestTransactionEntry(foodAccount, "9.99", false),
                new TestTransactionEntry(cashAccount,  "-9.99", false));
    }

    Transaction createTransactionWithUser(String name, User user) {
        return testSetup.createTransaction(name, user, LocalDate.of(2020, 1, 1),
                new TestTransactionEntry(foodAccount, "9.99", false),
                new TestTransactionEntry(cashAccount,  "-9.99", false));
    }

    Transaction createTransactionWithAmount(String name, String amount) {
        return testSetup.createTransaction(name, loggedInUser, LocalDate.of(2020, 1, 1),
                new TestTransactionEntry(foodAccount, amount, false),
                new TestTransactionEntry(cashAccount,  "-" + amount, false));
    }

    void createTransactionWithDate(String name, LocalDate date) {
        testSetup.createTransaction(name, loggedInUser, date,
                new TestTransactionEntry(foodAccount, "9.99", false),
                new TestTransactionEntry(cashAccount,  "-9.99", false));
    }

    void createTransactionWithAccounts(String name, Account debitAccount, Account creditAccount) {
        testSetup.createTransaction(name, loggedInUser, LocalDate.of(2020, 1, 1),
                new TestTransactionEntry(debitAccount, "9.99", false),
                new TestTransactionEntry(creditAccount,  "-9.99", false));
    }

    void createTransactionWithAccounts(String name, List<Account> debitAccounts, List<Account> creditAccounts) {
        var debitEntries = debitAccounts.stream()
                .map(account -> new TestTransactionEntry(account, String.valueOf(creditAccounts.size()), true));
        var creditEntries = creditAccounts.stream()
                .map(account -> new TestTransactionEntry(account, String.valueOf(debitAccounts.size()), true));

        testSetup.createTransaction(name, loggedInUser, LocalDate.of(2020, 1, 1),
                Stream.concat(debitEntries, creditEntries).toArray(TestTransactionEntry[]::new));
    }
}
