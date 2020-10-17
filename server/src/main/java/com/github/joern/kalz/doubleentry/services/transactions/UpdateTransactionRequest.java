package com.github.joern.kalz.doubleentry.services.transactions;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class UpdateTransactionRequest {

    private long id;
    private LocalDate date;
    private String name;
    private List<RequestEntry> entries = new ArrayList<>();
}
