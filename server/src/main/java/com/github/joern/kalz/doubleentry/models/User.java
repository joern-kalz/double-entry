package com.github.joern.kalz.doubleentry.models;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User implements Serializable {

    @Id
    private String username;

    private String password;

    private boolean enabled;

    @OneToMany(mappedBy = "id.user", cascade = CascadeType.REMOVE)
    private List<Authority> authorities;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<Account> accounts;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<Transaction> transactions;
}
