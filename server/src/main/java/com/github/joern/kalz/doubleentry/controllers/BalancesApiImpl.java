package com.github.joern.kalz.doubleentry.controllers;

import com.github.joern.kalz.doubleentry.generated.api.BalancesApi;
import com.github.joern.kalz.doubleentry.generated.model.GetBalanceResponse;
import com.github.joern.kalz.doubleentry.services.balances.BalancesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class BalancesApiImpl implements BalancesApi {

    @Autowired
    private BalancesService balancesService;

    @Override
    public ResponseEntity<List<GetBalanceResponse>> getBalances(@Valid LocalDate after, @Valid LocalDate before) {
        List<GetBalanceResponse> balances = balancesService.getBalances(after, before).entrySet().stream()
                .map(balance -> new GetBalanceResponse()
                        .accountId(balance.getKey())
                        .balance(balance.getValue()))
                .collect(Collectors.toList());

        return new ResponseEntity<>(balances, HttpStatus.OK);
    }
}