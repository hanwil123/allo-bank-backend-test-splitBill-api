package com.allobank.splitbill.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne
    @JoinColumn(name = "from_participant_id", nullable = false)
    private Participant from;

    @ManyToOne
    @JoinColumn(name = "to_participant_id", nullable = false)
    private Participant to;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, updatable = false)
    private Instant paidAt = Instant.now();

    protected Payment() {
        // JPA
    }

    public Payment(Group group, Participant from, Participant to, BigDecimal amount) {
        this.group = group;
        this.from = from;
        this.to = to;
        this.amount = amount;
    }

    public UUID getId() {
        return id;
    }

    public Group getGroup() {
        return group;
    }

    public Participant getFrom() {
        return from;
    }

    public Participant getTo() {
        return to;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Instant getPaidAt() {
        return paidAt;
    }
}
