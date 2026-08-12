package com.allobank.splitbill.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "participant")
public class Participant {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    protected Participant() {
    }

    public Participant(String name) {
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }
}
