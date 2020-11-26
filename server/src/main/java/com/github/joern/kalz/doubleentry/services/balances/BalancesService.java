package com.github.joern.kalz.doubleentry.services.balances;

import com.github.joern.kalz.doubleentry.models.Account;
import com.github.joern.kalz.doubleentry.models.TransactionSearchCriteria;
import com.github.joern.kalz.doubleentry.models.TransactionsRepository;
import com.github.joern.kalz.doubleentry.services.PrincipalProvider;
import com.github.joern.kalz.doubleentry.services.accounts.AccountsHierarchyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class BalancesService {

    @Autowired
    private TransactionsRepository transactionsRepository;

    @Autowired
    private AccountsHierarchyService accountsHierarchyService;

    @Autowired
    private PrincipalProvider principalProvider;

    public Map<Long, BigDecimal> getBalances(LocalDate after, LocalDate before) {
        Map<Long, BigDecimal> balances = getSeparateBalances(after, before);
        return getAddedUpBalances(balances);
    }

    private Map<Long, BigDecimal> getSeparateBalances(LocalDate after, LocalDate before) {
        Map<Long, BigDecimal> balances = new HashMap<>();

        TransactionSearchCriteria criteria = new TransactionSearchCriteria();
        criteria.setUser(principalProvider.getPrincipal());
        criteria.setAfter(after);
        criteria.setBefore(before);

        transactionsRepository
                .find(criteria)
                .stream()
                .flatMap(transaction -> transaction.getEntries().stream())
                .forEach(entry -> {
                    Account account = entry.getId().getAccount();
                    BigDecimal oldBalance = balances.getOrDefault(account.getId(), BigDecimal.ZERO);
                    balances.put(account.getId(), oldBalance.add(entry.getAmount()));
                });

        return balances;
    }

    private Map<Long, BigDecimal> getAddedUpBalances(Map<Long, BigDecimal> balances) {
        Map<Long, BigDecimal> addedUpBalances = new HashMap<>();

        accountsHierarchyService.walkHierarchyDepthFirst(account -> {
            BigDecimal amount = balances.getOrDefault(account.getId(), BigDecimal.ZERO);
            boolean childBalanceFound = false;

            for (Account child : account.getChildren()) {
                if (addedUpBalances.containsKey(child.getId())) {
                    childBalanceFound = true;
                    amount = amount.add(addedUpBalances.get(child.getId()));
                }
            }

            if (balances.containsKey(account.getId()) || childBalanceFound) {
                addedUpBalances.put(account.getId(), amount);
            }
        });

        return addedUpBalances;
    }
}
