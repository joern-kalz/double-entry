package com.github.joern.kalz.doubleentry.services.verifications;

import com.github.joern.kalz.doubleentry.models.Transaction;
import com.github.joern.kalz.doubleentry.models.TransactionsRepository;
import com.github.joern.kalz.doubleentry.services.PrincipalProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Set;

@Service
public class VerificationsService {
    @Autowired
    private TransactionsRepository transactionsRepository;

    @Autowired
    private PrincipalProvider principalProvider;

    public VerificationState getVerificationState(Long accountId) {
        Set<Transaction> transactions = transactionsRepository
                .findByUserAndDateAndAccount(principalProvider.getPrincipal(), null, null, accountId);

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
}
