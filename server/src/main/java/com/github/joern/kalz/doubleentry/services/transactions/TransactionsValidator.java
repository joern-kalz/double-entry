package com.github.joern.kalz.doubleentry.services.transactions;

import com.github.joern.kalz.doubleentry.models.Entry;
import com.github.joern.kalz.doubleentry.models.Transaction;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Service
public class TransactionsValidator {

    public enum Result {OK, LESS_THAN_TWO_ENTRIES, SAME_ACCOUNT_IN_TWO_OR_MORE_ENTRIES, TOTAL_NOT_ZERO}

    public Result validate(Transaction transaction) {
        if (transaction.getEntries().size() < 2) {
            return Result.LESS_THAN_TWO_ENTRIES;
        } else if (isSameAccountInTwoOrMoreEntries(transaction)) {
            return Result.SAME_ACCOUNT_IN_TWO_OR_MORE_ENTRIES;
        } else if (isTotalNotZero(transaction)) {
            return Result.TOTAL_NOT_ZERO;
        }

        return Result.OK;
    }

    private boolean isSameAccountInTwoOrMoreEntries(Transaction transaction) {
        Set<Long> accountIds = new HashSet<>();

        for (Entry entry : transaction.getEntries()) {
            Long accountId = entry.getId().getAccount().getId();

            if (accountIds.contains(accountId)) {
                return true;
            }

            accountIds.add(accountId);
        }

        return false;
    }

    private boolean isTotalNotZero(Transaction transaction) {
        return transaction.getEntries().stream()
                .map(Entry::getAmount)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO)
                .compareTo(BigDecimal.ZERO) != 0;
    }

}
