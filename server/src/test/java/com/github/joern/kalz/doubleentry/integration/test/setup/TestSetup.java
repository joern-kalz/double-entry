package com.github.joern.kalz.doubleentry.integration.test.setup;

import com.github.joern.kalz.doubleentry.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void clearDatabase() {
        transactionsRepository.deleteAll();
        accountsRepository.deleteAll();
        usersRepository.deleteAll();
    }

    public User createUser(String name, String password) {
        return usersRepository.save(new User(name, passwordEncoder.encode(password), true));
    }

    public Account createAccount(String name, User user) {
        Account account = new Account();
        account.setUser(user);
        account.setName(name);
        return accountsRepository.save(account);
    }

    public Account createParentChildRelationship(Account parent, Account child) {
        child.setParent(parent);
        return accountsRepository.save(child);
    }

    public Transaction createTransaction(String name, User user, LocalDate date, TestTransactionEntry... entries) {
        Transaction transaction = new Transaction();
        transaction.setName(name);
        transaction.setDate(date);
        transaction.setUser(user);

        List<Entry> entryList = Arrays.stream(entries)
                .map(entry -> new Entry(transaction, entry.getAccount(), new BigDecimal(entry.getAmount()),
                        entry.isVerified()))
                .collect(Collectors.toList());

        transaction.setEntries(entryList);

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

    public MockMvc createMockMvc() {
        return MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .defaultRequest(get("/")
                        .contentType(MediaType.APPLICATION_JSON))
                .alwaysDo(MockMvcResultHandlers.print())
                .build();
    }
}
