package com.github.joern.kalz.doubleentry.models;

import lombok.Data;

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;
import java.io.Serializable;

@Embeddable
@Data
public class EntryId implements Serializable {

    @ManyToOne
    private Transaction transaction;

    @ManyToOne
    private Account account;
}
