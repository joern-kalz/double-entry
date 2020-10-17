package com.github.joern.kalz.doubleentry.services.verifications;

import lombok.Data;

import java.util.List;

@Data
public class UpdateVerificationStateRequest {
    private Long accountId;
    private List<Long> transactionIds;
}
