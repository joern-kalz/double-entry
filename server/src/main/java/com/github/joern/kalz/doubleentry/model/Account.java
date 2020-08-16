package com.github.joern.kalz.doubleentry.model;

import javax.persistence.*;
import java.util.List;

@Entity
public class Account {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Account parent;

    @OneToMany(mappedBy = "parent")
    private List<Account> children;

    private String name;
    private boolean active;

    public Account() {
    }

    public Account(Account parent, String name, boolean active) {
        this.parent = parent;
        this.name = name;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Account getParent() {
        return parent;
    }

    public void setParent(Account parent) {
        this.parent = parent;
    }

    public List<Account> getChildren() {
        return children;
    }

    public void setChildren(List<Account> children) {
        this.children = children;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
