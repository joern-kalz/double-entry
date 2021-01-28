package com.github.joern.kalz.doubleentry.services.balances;

import com.github.joern.kalz.doubleentry.models.*;
import com.github.joern.kalz.doubleentry.services.PrincipalProvider;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BalancesService {

    private final TransactionsRepository transactionsRepository;
    private final AccountsRepository accountsRepository;
    private final PrincipalProvider principalProvider;

    public BalancesService(TransactionsRepository transactionsRepository, AccountsRepository accountsRepository,
                           PrincipalProvider principalProvider) {
        this.transactionsRepository = transactionsRepository;
        this.accountsRepository = accountsRepository;
        this.principalProvider = principalProvider;
    }

    public List<Balances> getBalances(LocalDate date, int stepInMonth, int stepCount) {
        LocalDate before = date.plusMonths((long) stepInMonth * stepCount);
        Map<Long, TurnoverIterator> turnoverIterators = getTurnoverIteratorsByAccountId(null, before);

        return getBalances(turnoverIterators, date, stepInMonth, stepCount, true);
    }

    public List<BalanceDifferences> getBalanceDifferences(LocalDate start, int stepInMonth, int stepCount) {
        LocalDate before = start.plusMonths((long) stepInMonth * stepCount);
        Map<Long, TurnoverIterator> turnoverIterators = getTurnoverIteratorsByAccountId(start, before);

        return getBalances(turnoverIterators, start.plusMonths(stepInMonth), stepInMonth, stepCount - 1, false)
                .stream()
                .map(balances -> new BalanceDifferences(
                        balances.getDate().minusMonths(stepInMonth),
                        balances.getDate().minusDays(1),
                        balances.getAmountsByAccountId()))
                .collect(Collectors.toList());
    }

    private List<Balances> getBalances(Map<Long, TurnoverIterator> turnoversByAccountId, LocalDate start,
                                       int stepInMonth, int stepCount, boolean accumulate) {
        List<Balances> balancesList = new ArrayList<>();

        for (int step = 0; step <= stepCount; step++) {
            LocalDate stepEnd = start.plusMonths((long) stepInMonth * step);
            Map<Long, BigDecimal> amountsByAccountId = accumulate && !balancesList.isEmpty() ?
                    new HashMap<>(balancesList.get(balancesList.size() - 1).getAmountsByAccountId()) :
                    turnoversByAccountId.keySet().stream().collect(Collectors.toMap(id -> id, id -> BigDecimal.ZERO));

            for (Map.Entry<Long, TurnoverIterator> turnovers : turnoversByAccountId.entrySet()) {
                Long accountId = turnovers.getKey();

                for (TurnoverIterator iterator = turnovers.getValue(); !iterator.isAfterLast(); iterator.moveNext()) {
                    Turnover turnover = iterator.get();

                    if (turnover.date.isAfter(stepEnd) || (!accumulate && turnover.date.isEqual(stepEnd))) {
                        break;
                    }

                    amountsByAccountId.put(accountId, amountsByAccountId.get(accountId).add(turnover.amount));
                }
            }

            balancesList.add(new Balances(stepEnd, amountsByAccountId));
        }

        return balancesList;
    }

    private Map<Long, TurnoverIterator> getTurnoverIteratorsByAccountId(LocalDate after, LocalDate before) {
        Map<Long, Long> parentsByAccountId = getParentsByAccountId();
        Map<Long, SortedMap<LocalDate, Turnover>> turnoversByAccount = new HashMap<>();

        for (Transaction transaction : getTransactions(after, before)) {
            LocalDate date = transaction.getDate();

            for (Entry entry : transaction.getEntries()) {
                Long accountId = entry.getId().getAccount().getId();

                do {
                    SortedMap<LocalDate, Turnover> turnovers = turnoversByAccount
                            .computeIfAbsent(accountId, k -> new TreeMap<>());

                    Turnover turnover = turnovers.computeIfAbsent(date, k -> new Turnover(date));
                    turnover.amount = turnover.amount.add(entry.getAmount());

                    accountId = parentsByAccountId.get(accountId);
                } while (accountId != null);
            }
        }

        return turnoversByAccount.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> new TurnoverIterator(entry.getValue())));
    }

    private List<Transaction> getTransactions(LocalDate after, LocalDate before) {
        TransactionSearchCriteria criteria = new TransactionSearchCriteria();
        criteria.setUser(principalProvider.getPrincipal());
        criteria.setAfter(after);
        criteria.setBefore(before);

        return transactionsRepository.find(criteria);
    }

    private Map<Long, Long> getParentsByAccountId() {
        Map<Long, Long> parentsByAccountId = new HashMap<>();

        for (Account account : accountsRepository.findByUser(principalProvider.getPrincipal())) {
            parentsByAccountId.put(account.getId(), account.getParent() != null ? account.getParent().getId() : null);
        }

        return parentsByAccountId;
    }

    private static class Turnover {
        LocalDate date;
        BigDecimal amount;

        Turnover(LocalDate date) {
            this.date = date;
            this.amount = BigDecimal.ZERO;
        }
    }

    private static class TurnoverIterator {
        int index;
        List<Turnover> turnovers;

        TurnoverIterator(SortedMap<LocalDate, Turnover> turnovers) {
            index = 0;
            this.turnovers = new ArrayList<>(turnovers.values());
        }

        boolean isAfterLast() {
            return index >= turnovers.size();
        }

        void moveNext() {
            index++;
        }

        Turnover get() {
            return turnovers.get(index);
        }
    }
}
