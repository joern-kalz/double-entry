package com.github.joern.kalz.doubleentry.models;

import lombok.Data;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;

@Embeddable
@Data
public class AuthorityId implements Serializable {

    @ManyToOne
    @JoinColumn(name = "username")
    private User user;

    private String authority;
}
