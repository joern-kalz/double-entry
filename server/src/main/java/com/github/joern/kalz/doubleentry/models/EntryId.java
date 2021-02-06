package com.github.joern.kalz.doubleentry.models;

import lombok.Data;

import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import java.io.Serializable;

@Embeddable
@Data
public class EntryId implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    private Transaction transaction;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account account;
}
