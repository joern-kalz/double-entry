package com.github.joern.kalz.doubleentry.controllers;

import com.github.joern.kalz.doubleentry.generated.api.VerificationsApi;
import com.github.joern.kalz.doubleentry.generated.model.ApiTransaction;
import com.github.joern.kalz.doubleentry.generated.model.ApiGetVerificationResponse;
import com.github.joern.kalz.doubleentry.services.verifications.UpdateVerificationStateRequest;
import com.github.joern.kalz.doubleentry.services.verifications.VerificationState;
import com.github.joern.kalz.doubleentry.services.verifications.VerificationsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class VerificationsApiImpl implements VerificationsApi {
    @Autowired
    private VerificationsService verificationsService;

    @Autowired
    private ResponseFactory responseFactory;

    @Override
    public ResponseEntity<ApiGetVerificationResponse> getVerification(Long accountId) {
        VerificationState verificationState = verificationsService.getVerificationState(accountId);

        List<ApiTransaction> unverifiedTransactions = verificationState.getUnverifiedTransactions().stream()
                .map(responseFactory::convertToResponse)
                .collect(Collectors.toList());

        ApiGetVerificationResponse response = new ApiGetVerificationResponse()
                .verifiedBalance(verificationState.getVerifiedBalance())
                .unverifiedTransactions(unverifiedTransactions);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> updateVerification(Long accountId, @Valid List<Long> requestBody) {
        UpdateVerificationStateRequest updateVerificationStateRequest = new UpdateVerificationStateRequest();
        updateVerificationStateRequest.setAccountId(accountId);
        updateVerificationStateRequest.setTransactionIds(requestBody);
        verificationsService.updateVerificationState(updateVerificationStateRequest);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
