package com.github.joern.kalz.doubleentry.controllers;

import com.github.joern.kalz.doubleentry.generated.api.PortfolioReturnsApi;
import com.github.joern.kalz.doubleentry.generated.model.ApiGetPortfolioReturnsResponse;
import com.github.joern.kalz.doubleentry.services.returns.PortfolioReturnPeriod;
import com.github.joern.kalz.doubleentry.services.returns.PortfolioReturnsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class PortfolioReturnsApiImpl implements PortfolioReturnsApi {

    private final PortfolioReturnsService portfolioReturnsService;

    public PortfolioReturnsApiImpl(PortfolioReturnsService portfolioReturnsService) {
        this.portfolioReturnsService = portfolioReturnsService;
    }

    @Override
    public ResponseEntity<List<ApiGetPortfolioReturnsResponse>> getPortfolioReturns(
            @NotNull @Valid Long portfolioAccountId,
            @NotNull @Valid Long revenueAccountId,
            @NotNull @Valid Long expenseAccountId,
            @NotNull @Valid LocalDate until,
            @NotNull @Min(1) @Valid Integer stepYears) {

        var portfolioReturns = portfolioReturnsService
                .getReturnPeriods(portfolioAccountId, revenueAccountId, expenseAccountId, until, stepYears)
                .stream()
                .map(this::convertPortfolioReturnPeriod)
                .collect(Collectors.toList());

        return new ResponseEntity<>(portfolioReturns, HttpStatus.OK);
    }

    private ApiGetPortfolioReturnsResponse convertPortfolioReturnPeriod(PortfolioReturnPeriod period) {
        return new ApiGetPortfolioReturnsResponse()
                .start(period.getStart())
                .end(period.getEnd())
                .portfolioReturn(period.getPortfolioReturn());
    }
}
