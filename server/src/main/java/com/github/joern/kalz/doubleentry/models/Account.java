package com.github.joern.kalz.doubleentry.models;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Getter
@Setter
public class Account implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Account parent;

    @OneToMany(mappedBy = "parent")
    private List<Account> children;

    private String name;
    private boolean active;
}
