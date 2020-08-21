package com.github.joern.kalz.doubleentry.integration.test.setup;

import com.github.joern.kalz.doubleentry.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@Service
public class TestSetup {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private AccountsRepository accountsRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private TransactionsRepository transactionsRepository;

    public void clearDatabase() {
        transactionsRepository.deleteAll();
        usersRepository.deleteAll();
    }

    public User createUser(String name) {
        return usersRepository.save(new User(name, null, true));
    }

    public Account createAccount(String name, User user, Account parent) {
        Account account = new Account();
        account.setUser(user);
        account.setName(name);
        account.setParent(parent);
        return accountsRepository.save(account);
    }

    public Transaction createTransaction(String name, User user, LocalDate date, Account debitAccount,
                                         String debitAmount, Account creditAccount, String creditAmount) {
        Transaction transaction = new Transaction();
        transaction.setName(name);
        transaction.setDate(date);
        transaction.setUser(user);

        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(transaction, debitAccount, new BigDecimal(debitAmount), false));
        entries.add(new Entry(transaction, creditAccount, new BigDecimal(creditAmount), false));
        transaction.setEntries(entries);

        return transactionsRepository.save(transaction);
    }

    public MockMvc createAuthenticatedMockMvc(User loggedInUser) {
        return MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .defaultRequest(get("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(loggedInUser.getUsername())).with(csrf()))
                .alwaysDo(MockMvcResultHandlers.print())
                .build();
    }

}
