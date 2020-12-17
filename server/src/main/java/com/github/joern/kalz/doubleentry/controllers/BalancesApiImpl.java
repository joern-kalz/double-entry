package com.github.joern.kalz.doubleentry.controllers;

import com.github.joern.kalz.doubleentry.generated.api.BalancesApi;
import com.github.joern.kalz.doubleentry.generated.model.ApiGetAbsoluteBalanceResponse;
import com.github.joern.kalz.doubleentry.generated.model.ApiGetAbsoluteBalanceResponseBalances;
import com.github.joern.kalz.doubleentry.generated.model.ApiGetRelativeBalanceResponse;
import com.github.joern.kalz.doubleentry.services.balances.BalanceDifferences;
import com.github.joern.kalz.doubleentry.services.balances.Balances;
import com.github.joern.kalz.doubleentry.services.balances.BalancesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class BalancesApiImpl implements BalancesApi {

    private final BalancesService balancesService;

    public BalancesApiImpl(BalancesService balancesService) {
        this.balancesService = balancesService;
    }

    @Override
    public ResponseEntity<List<ApiGetAbsoluteBalanceResponse>> getAbsoluteBalances(@NotNull @Valid LocalDate date,
        @Min(1) @Valid Integer stepMonths, @Min(0) @Valid Integer stepCount) {

        Integer stepMonthOrNull = Optional.ofNullable(stepMonths).orElse(0);
        Integer stepCountOrNull = Optional.ofNullable(stepCount).orElse(0);
        List<Balances> balancesList = balancesService.getBalances(date, stepMonthOrNull, stepCountOrNull);
        return new ResponseEntity<>(convertToAbsoluteResponse(balancesList), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<ApiGetRelativeBalanceResponse>> getRelativeBalances(@NotNull @Valid LocalDate start,
        @NotNull @Min(1) @Valid Integer stepMonths, @NotNull @Min(1) @Valid Integer stepCount) {

        List<BalanceDifferences> differences = balancesService.getBalanceDifferences(start, stepMonths, stepCount);
        return new ResponseEntity<>(convertToRelativeResponse(differences), HttpStatus.OK);
    }

    private List<ApiGetRelativeBalanceResponse> convertToRelativeResponse(List<BalanceDifferences> differencesList) {
        return differencesList.stream()
                .map(differences -> new ApiGetRelativeBalanceResponse()
                        .start(differences.getStart())
                        .end(differences.getEnd())
                        .differences(convertToResponse(differences.getAmountsByAccountId())))
                .collect(Collectors.toList());
    }

    private List<ApiGetAbsoluteBalanceResponse> convertToAbsoluteResponse(List<Balances> balancesList) {
        return balancesList.stream()
                .map(balances -> new ApiGetAbsoluteBalanceResponse()
                        .date(balances.getDate())
                        .balances(convertToResponse(balances.getAmountsByAccountId())))
                .collect(Collectors.toList());
    }

    private List<ApiGetAbsoluteBalanceResponseBalances> convertToResponse(Map<Long, BigDecimal> amounts) {
        return amounts.entrySet().stream()
                .map(amount -> new ApiGetAbsoluteBalanceResponseBalances()
                        .accountId(amount.getKey())
                        .amount(amount.getValue()))
                .collect(Collectors.toList());
    }
}
