package com.allobank.splitbill.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "expense_share")
public class ExpenseShare {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "expense_id", nullable = false)
    private Expense expense;

    @ManyToOne
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    protected ExpenseShare() {
    }

    public ExpenseShare(Participant participant, BigDecimal amount) {
        this.participant = participant;
        this.amount = amount;
    }

    public UUID getId() {
        return id;
    }

    public Expense getExpense() {
        return expense;
    }

    public void setExpense(Expense expense) {
        this.expense = expense;
    }

    public Participant getParticipant() {
        return participant;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
