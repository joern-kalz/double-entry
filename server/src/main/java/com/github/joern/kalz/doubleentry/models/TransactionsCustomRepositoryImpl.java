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
                                                        List<Long> accountIds) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Transaction> query = cb.createQuery(Transaction.class);
        Root<Transaction> transaction = query.from(Transaction.class);
        List<Predicate> predicates = new ArrayList<>();
        transaction.fetch("entries", JoinType.INNER);

        if (user != null) {
            predicates.add(cb.equal(transaction.get("user"), user));
        }

        if (after != null) {
            predicates.add(cb.greaterThanOrEqualTo(transaction.get("date"), after));
        }

        if (before != null) {
            predicates.add(cb.lessThanOrEqualTo(transaction.get("date"), before));
        }

        if (accountIds != null) {
            Join<Object, Entry> entry = transaction.join("entries");
            CriteriaBuilder.In<Object> in = cb.in(entry.get("id").get("account").get("id"));
            accountIds.forEach(in::value);
            predicates.add(in);
        }

        Predicate[] predicateArray = predicates.toArray(new Predicate[0]);

        query.select(transaction)
                .where(cb.and(predicateArray))
                .orderBy(cb.asc(transaction.get("date")));

        return new HashSet<>(entityManager.createQuery(query).getResultList());
    }
}
