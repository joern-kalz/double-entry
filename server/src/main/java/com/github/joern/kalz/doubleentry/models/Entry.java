package com.github.joern.kalz.doubleentry.models;

import lombok.*;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@ToString
public class Entry implements Serializable {

    @EmbeddedId
    private EntryId id;

    private BigDecimal amount;
    private boolean verified;
}
