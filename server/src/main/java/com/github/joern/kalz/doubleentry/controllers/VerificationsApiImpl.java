package com.github.joern.kalz.doubleentry.controllers;

import com.github.joern.kalz.doubleentry.generated.api.VerificationsApi;
import com.github.joern.kalz.doubleentry.generated.model.GetTransactionResponse;
import com.github.joern.kalz.doubleentry.generated.model.GetVerificationResponse;
import com.github.joern.kalz.doubleentry.services.verifications.VerificationState;
import com.github.joern.kalz.doubleentry.services.verifications.VerificationsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class VerificationsApiImpl implements VerificationsApi {
    @Autowired
    private VerificationsService verificationsService;

    @Autowired
    private TransactionResponseFactory transactionResponseFactory;

    @Override
    public ResponseEntity<GetVerificationResponse> getVerification(Long accountId) {
        VerificationState verificationState = verificationsService.getVerificationState(accountId);

        List<GetTransactionResponse> unverifiedTransactions = verificationState.getUnverifiedTransactions().stream()
                .map(transactionResponseFactory::convertToResponse)
                .collect(Collectors.toList());

        GetVerificationResponse response = new GetVerificationResponse()
                .verifiedBalance(verificationState.getVerifiedBalance())
                .unverifiedTransactions(unverifiedTransactions);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> updateVerification(Long accountId, @Valid List<Long> requestBody) {
        return null;
    }
}