package com.github.joern.kalz.doubleentry.models;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.util.*;
import java.util.stream.Collectors;

public class TransactionsCustomRepositoryImpl implements TransactionsCustomRepository {

    private enum AccountType { CREDIT, DEBIT, ANY }
    private final EntityManager entityManager;

    public TransactionsCustomRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<Transaction> find(TransactionSearchCriteria request) {
        HashSet<Transaction> transactions = getTransactions(request);

        var filteredTransactions = filterTransactions(transactions, request);

        var sortedTransactions = new TreeSet<>(getTransactionComparator(request.getOrder()));
        sortedTransactions.addAll(filteredTransactions);

        return getPage(sortedTransactions, request);
    }

    private HashSet<Transaction> getTransactions(TransactionSearchCriteria request) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Transaction> query = cb.createQuery(Transaction.class);

        Root<Transaction> transaction = query.from(Transaction.class);
        transaction.fetch("entries", JoinType.INNER);

        query.select(transaction)
                .where(cb.and(getPredicates(request, cb, transaction)));

        return new HashSet<>(entityManager.createQuery(query).getResultList());
    }

    private Predicate[] getPredicates(TransactionSearchCriteria request, CriteriaBuilder cb,
                                      Root<Transaction> transaction) {

        List<Predicate> predicates = new ArrayList<>();

        if (request.getUser() != null) {
            predicates.add(cb.equal(transaction.get("user"), request.getUser()));
        }

        if (request.getAfter() != null) {
            predicates.add(cb.greaterThanOrEqualTo(transaction.get("date"), request.getAfter()));
        }

        if (request.getBefore() != null) {
            predicates.add(cb.lessThanOrEqualTo(transaction.get("date"), request.getBefore()));
        }

        if (request.getName() != null && request.getName().length() > 0) {
            predicates.add(cb.like(transaction.get("name"), request.getName().replace('*', '%')));
        }

        return predicates.toArray(new Predicate[0]);
    }

    private Set<Transaction> filterTransactions(Set<Transaction> transactions, TransactionSearchCriteria request) {
        return transactions.stream()
                .filter(transaction -> hasTransactionMatchingAccount(transaction, request.getAccountIds(),
                        AccountType.ANY))
                .filter(transaction -> hasTransactionMatchingAccount(transaction, request.getCreditAccountIds(),
                        AccountType.CREDIT))
                .filter(transaction -> hasTransactionMatchingAccount(transaction, request.getDebitAccountIds(),
                        AccountType.DEBIT))
                .collect(Collectors.toSet());
    }

    private boolean hasTransactionMatchingAccount(Transaction transaction, List<Long> accounts, AccountType type) {
        if (accounts == null) {
            return true;
        }

        Set<Long> transactionAccounts = getAccountsWithType(transaction, type);
        return accounts.stream().anyMatch(transactionAccounts::contains);
    }

    private Set<Long> getAccountsWithType(Transaction transaction, AccountType accountType) {
        return transaction.getEntries().stream()
                .filter(entry -> isEntryMatchingType(entry, accountType))
                .map(entry -> entry.getId().getAccount().getId())
                .collect(Collectors.toSet());
    }

    private boolean isEntryMatchingType(Entry entry, AccountType accountType) {
        return accountType == AccountType.ANY ||
                (accountType == AccountType.CREDIT && entry.getAmount().signum() == -1) ||
                (accountType == AccountType.DEBIT && entry.getAmount().signum() == 1);
    }

    private Comparator<Transaction> getTransactionComparator(TransactionOrder order) {
        if (order == null || order == TransactionOrder.DATE_ASCENDING) {
            return Comparator.comparing(Transaction::getDate).thenComparing(Transaction::getId);
        } else {
            return Comparator.comparing(Transaction::getDate).reversed().thenComparing(Transaction::getId);
        }
    }

    private List<Transaction> getPage(TreeSet<Transaction> sortedTransactions, TransactionSearchCriteria request) {
        if (request.getMaxPageSize() == null) {
            return new ArrayList<>(sortedTransactions);
        }

        int fromIndex = request.getPageOffset() != null ? request.getPageOffset() * request.getMaxPageSize() : 0;

        if (fromIndex >= sortedTransactions.size()) {
            return Collections.emptyList();
        }

        int toIndex = Math.min(fromIndex + request.getMaxPageSize(), sortedTransactions.size());

        return new ArrayList<>(sortedTransactions).subList(fromIndex, toIndex);
    }
}
