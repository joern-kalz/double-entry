package com.github.joern.kalz.doubleentry.models;

import lombok.*;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "authorities")
@Getter
@Setter
@ToString
public class Authority implements Serializable {

    @EmbeddedId
    private AuthorityId id;
}
