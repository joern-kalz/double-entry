package com.github.joern.kalz.doubleentry.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@ToString
public class Transaction implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private User user;

    @OneToMany(mappedBy = "id.transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Entry> entries;

    private LocalDate date;
    private String name;
}
