package com.github.joern.kalz.doubleentry.model;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "authorities")
public class Authority {

    @EmbeddedId
    private AuthorityId id;

    public AuthorityId getId() {
        return id;
    }

    public void setId(AuthorityId id) {
        this.id = id;
    }
}
