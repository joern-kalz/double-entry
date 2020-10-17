package com.github.joern.kalz.doubleentry.services.verifications;

import com.github.joern.kalz.doubleentry.models.Entry;
import com.github.joern.kalz.doubleentry.models.Transaction;
import com.github.joern.kalz.doubleentry.models.TransactionsRepository;
import com.github.joern.kalz.doubleentry.services.PrincipalProvider;
import com.github.joern.kalz.doubleentry.services.exceptions.ParameterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.*;

@Service
public class VerificationsService {
    @Autowired
    private TransactionsRepository transactionsRepository;

    @Autowired
    private PrincipalProvider principalProvider;

    public VerificationState getVerificationState(Long accountId) {
        Set<Transaction> transactionsSet = transactionsRepository
                .findByUserAndDateAndAccount(principalProvider.getPrincipal(), null, null,
                        Collections.singletonList(accountId));

        List<Transaction> transactions = new ArrayList<>(transactionsSet);
        transactions.sort(Comparator.comparing(Transaction::getDate));

        VerificationState verificationState = new VerificationState();
        verificationState.setVerifiedBalance(BigDecimal.ZERO);
        verificationState.setUnverifiedTransactions(new ArrayList<>());

        transactions.stream()
                .flatMap(transaction -> transaction.getEntries().stream())
                .filter(entry -> entry.getId().getAccount().getId().equals(accountId))
                .forEach(entry -> {
                    if (entry.isVerified()) {
                        BigDecimal sum = verificationState.getVerifiedBalance().add(entry.getAmount());
                        verificationState.setVerifiedBalance(sum);
                    } else {
                        verificationState.getUnverifiedTransactions().add(entry.getId().getTransaction());
                    }
                });

        return verificationState;
    }

    @Transactional
    public void updateVerificationState(UpdateVerificationStateRequest updateVerificationStateRequest) {
        Set<Long> ids = new HashSet<>(updateVerificationStateRequest.getTransactionIds());

        for (Transaction transaction : transactionsRepository.findAllById(ids)) {
            if (!principalProvider.getPrincipal().equals(transaction.getUser())) {
                throw createParameterException(transaction.getId(), "not found");
            }

            if (!verify(transaction, updateVerificationStateRequest.getAccountId())) {
                throw createParameterException(transaction.getId(), "has no entry with account " +
                        updateVerificationStateRequest.getAccountId());
            }

            transactionsRepository.save(transaction);
            ids.remove(transaction.getId());
        }

        if (!ids.isEmpty()) {
            throw createParameterException(ids.iterator().next(), "not found");
        }
    }

    private boolean verify(Transaction transaction, Long accountId) {
        for (Entry entry : transaction.getEntries()) {
            if (entry.getId().getAccount().getId().equals(accountId)) {
                entry.setVerified(true);
                return true;
            }
        }

        return false;
    }

    private ParameterException createParameterException(long transactionId, String message) {
        return new ParameterException("transaction " + transactionId + " " + message);
    }
}
