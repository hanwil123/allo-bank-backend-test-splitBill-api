package com.allobank.splitbill.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "bill_group")
public class Group {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Participant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Expense> expenses = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Group() {
    }

    public Group(String name) {
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

    public List<Participant> getParticipants() {
        return participants;
    }

    public List<Expense> getExpenses() {
        return expenses;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void addParticipant(Participant participant) {
        participants.add(participant);
        participant.setGroup(this);
    }

    public void addExpense(Expense expense) {
        expenses.add(expense);
        expense.setGroup(this);
    }
}
