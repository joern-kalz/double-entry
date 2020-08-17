package com.github.joern.kalz.doubleentry.integration.test;

import com.github.joern.kalz.doubleentry.model.Account;
import com.github.joern.kalz.doubleentry.model.AccountsRepository;
import com.github.joern.kalz.doubleentry.model.Transaction;
import com.github.joern.kalz.doubleentry.model.TransactionsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TransactionsApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountsRepository accountsRepository;

    @Autowired
    private TransactionsRepository transactionsRepository;

    private long cashAccount;
    private long foodAccount;

    @BeforeEach
    public void createAccounts() {
        cashAccount = accountsRepository.save(new Account(null, "cash", true)).getId();
        foodAccount = accountsRepository.save(new Account(null, "food", true)).getId();
        transactionsRepository.deleteAll();
    }

    @Test
    @Transactional
    public void shouldCreateTransaction() throws Exception {
        String requestBody = "{\"name\":\"bread and butter\",\"date\":\"2020-01-01\",\"entries\":[" +
                "{\"accountId\":" + cashAccount + ",\"amount\":-9.99}," +
                "{\"accountId\":" + foodAccount + ",\"amount\":9.99}]}";

        mockMvc.perform(post("/transactions")
                .contentType(MediaType.APPLICATION_JSON).content(requestBody)
                .with(user("smith")).with(csrf()))
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

        mockMvc.perform(post("/transactions")
                .contentType(MediaType.APPLICATION_JSON).content(requestBody)
                .with(user("smith")).with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldNotCreateTransactionWithDuplicatedAccount() throws Exception {
        String requestBody = "{\"name\":\"bread and butter\",\"date\":\"2020-01-01\",\"entries\":[" +
                "{\"accountId\":" + cashAccount + ",\"amount\":-9.99}," +
                "{\"accountId\":" + cashAccount + ",\"amount\":9.99}]}";

        mockMvc.perform(post("/transactions")
                .contentType(MediaType.APPLICATION_JSON).content(requestBody)
                .with(user("smith")).with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldNotCreateTransactionWithNonZeroTotal() throws Exception {
        String requestBody = "{\"name\":\"bread and butter\",\"date\":\"2020-01-01\",\"entries\":[" +
                "{\"accountId\":" + cashAccount + ",\"amount\":-9.98}," +
                "{\"accountId\":" + foodAccount + ",\"amount\":9.99}]}";

        mockMvc.perform(post("/transactions")
                .contentType(MediaType.APPLICATION_JSON).content(requestBody)
                .with(user("smith")).with(csrf()))
                .andExpect(status().isBadRequest());
    }
}
