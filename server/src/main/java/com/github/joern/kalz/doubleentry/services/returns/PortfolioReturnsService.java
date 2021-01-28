package com.github.joern.kalz.doubleentry.services.returns;

import com.github.joern.kalz.doubleentry.models.*;
import com.github.joern.kalz.doubleentry.services.PrincipalProvider;
import com.github.joern.kalz.doubleentry.services.exceptions.NotFoundException;
import com.github.joern.kalz.doubleentry.services.exceptions.ParameterException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PortfolioReturnsService {

    private final AccountsRepository accountsRepository;
    private final TransactionsRepository transactionsRepository;
    private final PrincipalProvider principalProvider;

    public PortfolioReturnsService(AccountsRepository accountsRepository,
                                   TransactionsRepository transactionsRepository,
                                   PrincipalProvider principalProvider) {
        this.accountsRepository = accountsRepository;
        this.transactionsRepository = transactionsRepository;
        this.principalProvider = principalProvider;
    }

    public List<PortfolioReturnPeriod> getReturnPeriods(long portfolioAccountId, long revenueAccountId,
                                                        long expenseAccountId, LocalDate until, int stepYears) {
        var principal = principalProvider.getPrincipal();
        var accounts = accountsRepository.findByUser(principal);
        var portfolioAccounts = getAccountIdsRecursively(accounts, portfolioAccountId);
        var earningsAccounts = getEarningsAccounts(revenueAccountId, expenseAccountId, accounts);
        var depositAccounts = getDepositAccounts(accounts, portfolioAccounts, earningsAccounts);
        var transactions = getTransactions(until, principal, portfolioAccounts, depositAccounts);

        var balance = BigDecimal.ZERO;
        var i = 0;
        var periods = new ArrayList<PortfolioReturnPeriod>();
        var stepStart = getFirstStepStart(transactions.get(0).getDate(), until, stepYears);
        var stepEnd = getStepEnd(stepStart, stepYears, until);
        var deposits = new ArrayList<Deposit>();

        while (stepStart.isBefore(until)) {
            if (i < transactions.size() && transactions.get(i).getDate().isBefore(stepEnd)) {
                var earningsAmount = getTotalForAccountGroup(transactions.get(i), earningsAccounts);
                var depositAmount = getTotalForAccountGroup(transactions.get(i), depositAccounts);
                balance = balance.subtract(earningsAmount).subtract(depositAmount);

                if (!depositAmount.equals(BigDecimal.ZERO)) {
                    deposits.add(new Deposit(transactions.get(i).getDate(), -depositAmount.doubleValue()));
                }

                i++;
            } else {
                periods.add(getPortfolioReturn(deposits, stepEnd, balance.doubleValue()));
                stepStart = stepEnd;
                stepEnd = getStepEnd(stepStart, stepYears, until);
                deposits.clear();
                deposits.add(new Deposit(stepStart, balance.doubleValue()));
            }
        }

        return periods;
    }

    private Set<Long> getAccountIdsRecursively(List<Account> accounts, long rootAccountId) {
        Account rootAccount = accounts.stream()
                .filter(account -> account.getId() == rootAccountId)
                .findAny()
                .orElseThrow(() -> new NotFoundException("account " + rootAccountId + " not found"));

        Set<Long> visited = new HashSet<>();
        Deque<Account> toBeVisited = new ArrayDeque<>();
        toBeVisited.add(rootAccount);

        while (!toBeVisited.isEmpty()) {
            Account account = toBeVisited.removeFirst();

            if (!visited.contains(account.getId())) {
                visited.add(account.getId());
                toBeVisited.addAll(account.getChildren());
            }
        }

        return visited;
    }

    private Set<Long> getEarningsAccounts(long revenueAccountId, long expenseAccountId, List<Account> accounts) {
        Set<Long> earningsAccounts = getAccountIdsRecursively(accounts, revenueAccountId);
        earningsAccounts.addAll(getAccountIdsRecursively(accounts, expenseAccountId));
        return earningsAccounts;
    }

    private Set<Long> getDepositAccounts(List<Account> accounts, Set<Long> portfolioAccounts, Set<Long> earningsAccounts) {
        var depositAccounts = accounts.stream().map(Account::getId).collect(Collectors.toSet());
        depositAccounts.removeAll(portfolioAccounts);
        depositAccounts.removeAll(earningsAccounts);
        return depositAccounts;
    }

    private List<Transaction> getTransactions(LocalDate until, User principal, Set<Long> portfolioAccounts,
                                              Set<Long> depositAccounts) {

        var transactionSearchCriteria = new TransactionSearchCriteria();
        transactionSearchCriteria.setUser(principal);
        transactionSearchCriteria.setBefore(until);
        transactionSearchCriteria.setOrder(TransactionOrder.DATE_ASCENDING);
        transactionSearchCriteria.setAccountIds(new ArrayList<>(portfolioAccounts));
        var transactions = transactionsRepository.find(transactionSearchCriteria);

        if (transactions.isEmpty()) {
            throw new ParameterException("no transactions until " + until + " in portfolio account");
        } else if (getTotalForAccountGroup(transactions.get(0), depositAccounts).equals(BigDecimal.ZERO)) {
            throw new ParameterException("first transaction of portfolio account is not a deposit");
        }

        return transactions;
    }

    private LocalDate getFirstStepStart(LocalDate from, LocalDate until, int stepYears) {
        LocalDate start = until
                .with(TemporalAdjusters.firstDayOfYear())
                .minusYears(stepYears - 1L);

        while (start.isAfter(from)) {
            start = start.minusYears(stepYears);
        }

        return start;
    }

    private LocalDate getStepEnd(LocalDate stepStart, int stepYears, LocalDate until) {
        LocalDate stepEnd = stepStart.plusYears(stepYears);
        LocalDate maxStepEnd = until.plusDays(1);
        return stepEnd.isBefore(maxStepEnd) ? stepEnd : maxStepEnd;
    }

    private BigDecimal getTotalForAccountGroup(Transaction transaction, Set<Long> accountGroup) {
        BigDecimal total = BigDecimal.ZERO;

        for (var entry : transaction.getEntries()) {
            if (accountGroup.contains(entry.getId().getAccount().getId())) {
                total = total.add(entry.getAmount());
            }
        }

        return total;
    }

    private PortfolioReturnPeriod getPortfolioReturn(List<Deposit> deposits, LocalDate stepEndDate,
                                                     double stepEndAmount) {

        double returnFloor = -100;
        double returnCeiling = 10_000;

        if (getExpectedFinalAmount(deposits, stepEndDate, returnCeiling) < stepEndAmount) {
            throw new ParameterException("Maximum investment return " + returnCeiling + "% exceeded");
        }

        while (returnCeiling - returnFloor >= 0.05) {
            double returnMiddle = (returnCeiling + returnFloor) / 2.0;
            double expectedFinalAmount = getExpectedFinalAmount(deposits, stepEndDate, returnMiddle);

            if (expectedFinalAmount < stepEndAmount) {
                returnFloor = returnMiddle;
            } else {
                returnCeiling = returnMiddle;
            }
        }

        var portfolioReturn = new BigDecimal(String.format("%.1f", returnFloor));
        return new PortfolioReturnPeriod(deposits.get(0).date, stepEndDate.minusDays(1), portfolioReturn);
    }

    private double getExpectedFinalAmount(List<Deposit> deposits, LocalDate stepEndDate, double investmentReturn) {
        var finalAmount = 0.0;

        for (int i = 0; i < deposits.size(); i++) {
            finalAmount += deposits.get(i).amount;

            var intervalEnd = i < deposits.size() - 1 ? deposits.get(i + 1).date : stepEndDate;
            var duration = deposits.get(i).date.until(intervalEnd, ChronoUnit.DAYS);
            finalAmount *= Math.pow(1.0 + investmentReturn / 100.0, duration / 365.0);
        }

        return finalAmount;
    }

    @AllArgsConstructor
    private static class Deposit {
        LocalDate date;
        double amount;
    }
}
