package com.github.joern.kalz.doubleentry.models;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "authorities")
public class Authority implements Serializable {

    @EmbeddedId
    private AuthorityId id;

    public Authority() {
    }

    public Authority(User user, String authority) {
        id = new AuthorityId(user, authority);
    }

    public AuthorityId getId() {
        return id;
    }

    public void setId(AuthorityId id) {
        this.id = id;
    }
}
