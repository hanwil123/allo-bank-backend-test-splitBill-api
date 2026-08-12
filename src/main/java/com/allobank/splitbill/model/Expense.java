package com.allobank.splitbill.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "expense")
public class Expense {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne
    @JoinColumn(name = "paid_by_id", nullable = false)
    private Participant paidBy;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SplitType splitType;

    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExpenseShare> shares = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Expense() {
    }

    public Expense(Participant paidBy, BigDecimal amount, String description, ExpenseCategory category, SplitType splitType) {
        this.paidBy = paidBy;
        this.amount = amount;
        this.description = description;
        this.category = category;
        this.splitType = splitType;
    }

    public UUID getId() {
        return id;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public Participant getPaidBy() {
        return paidBy;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public ExpenseCategory getCategory() {
        return category;
    }

    public SplitType getSplitType() {
        return splitType;
    }

    public List<ExpenseShare> getShares() {
        return shares;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void addShare(ExpenseShare share) {
        shares.add(share);
        share.setExpense(this);
    }
}
