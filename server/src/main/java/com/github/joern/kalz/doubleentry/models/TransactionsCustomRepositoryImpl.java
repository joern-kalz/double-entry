package com.github.joern.kalz.doubleentry.models;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TransactionsCustomRepositoryImpl implements TransactionsCustomRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Set<Transaction> findByUserAndDateAndAccount(User user, LocalDate after, LocalDate before,
                                                        Long accountId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Transaction> query = cb.createQuery(Transaction.class);
        Root<Transaction> transaction = query.from(Transaction.class);
        List<Predicate> predicates = new ArrayList<>();

        if (user != null) {
            predicates.add(cb.equal(transaction.get("user"), user));
        }

        if (after != null) {
            predicates.add(cb.greaterThanOrEqualTo(transaction.get("date"), after));
        }

        if (before != null) {
            predicates.add(cb.lessThanOrEqualTo(transaction.get("date"), before));
        }

        if (accountId != null) {
            Join<Object, Entry> entry = transaction.join("entries");
            predicates.add(cb.equal(entry.get("id").get("account").get("id"), accountId));
        }

        Predicate[] predicateArray = predicates.toArray(new Predicate[0]);
        query.select(transaction).where(cb.and(predicateArray));

        return new HashSet<>(entityManager.createQuery(query).getResultList());
    }
}
