package com.github.joern.kalz.doubleentry.models;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.*;

public class TransactionsCustomRepositoryImpl implements TransactionsCustomRepository {

    private enum AccountType { CREDIT, DEBIT, ANY }
    private final EntityManager entityManager;

    public TransactionsCustomRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<Transaction> find(TransactionSearchCriteria request) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Transaction> query = cb.createQuery(Transaction.class);
        Root<Transaction> transaction = query.from(Transaction.class);
        transaction.fetch("entries", JoinType.INNER);
        Predicate[] predicateArray = getPredicates(request, cb, transaction);

        query.select(transaction).where(cb.and(predicateArray));

        return getTransactions(query, request.getPageOffset(), request.getMaxPageSize(), request.getOrder());
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

        if (request.getAccountIds() != null) {
            predicates.add(getAccountPredicate(request.getAccountIds(), AccountType.ANY, cb, transaction));
        }

        if (request.getCreditAccountIds() != null) {
            predicates.add(getAccountPredicate(request.getCreditAccountIds(), AccountType.CREDIT, cb, transaction));
        }

        if (request.getDebitAccountIds() != null) {
            predicates.add(getAccountPredicate(request.getDebitAccountIds(), AccountType.DEBIT, cb, transaction));
        }

        if (request.getName() != null && request.getName().length() > 0) {
            predicates.add(cb.like(transaction.get("name"), request.getName().replace('*', '%')));
        }

        return predicates.toArray(new Predicate[0]);
    }

    private Predicate getAccountPredicate(List<Long> accountIds, AccountType type,
        CriteriaBuilder cb, Root<Transaction> transaction) {

        Join<Object, Entry> entry = transaction.join("entries");
        CriteriaBuilder.In<Object> idPredicate = cb.in(entry.get("id").get("account").get("id"));
        accountIds.forEach(idPredicate::value);

        if (type == AccountType.ANY) {
            return idPredicate;
        }

        Predicate amountPredicate = type == AccountType.CREDIT ?
                cb.lessThan(entry.get("amount"), 0) :
                cb.greaterThan(entry.get("amount"), 0);

        return cb.and(idPredicate, amountPredicate);
    }

    private List<Transaction> getTransactions(CriteriaQuery<Transaction> query, Integer pageOffset,
        Integer maxPageSize, TransactionOrder order) {

        TypedQuery<Transaction> typedQuery = entityManager.createQuery(query);

        if (pageOffset != null) {
            typedQuery = typedQuery.setFirstResult(pageOffset);
        }

        if (maxPageSize != null) {
            typedQuery = typedQuery.setMaxResults(maxPageSize);
        }

        Set<Transaction> transactions = new TreeSet<>(getTransactionComparator(order));
        transactions.addAll(typedQuery.getResultList());

        return new ArrayList<>(transactions);
    }

    private Comparator<Transaction> getTransactionComparator(TransactionOrder order) {
        if (order == null || order == TransactionOrder.DATE_ASCENDING) {
            return Comparator.comparing(Transaction::getDate).thenComparing(Transaction::getId);
        } else {
            return Comparator.comparing(Transaction::getDate).reversed().thenComparing(Transaction::getId);
        }
    }
}
