package com.housemate.backend.model.expense;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

import com.housemate.backend.model.user.User;
import com.housemate.backend.model.household.Household;

@Entity
@Table(name = "debts")
@Getter
@Setter
@NoArgsConstructor
public class Debt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // The person who OWES money (Source)
    @ManyToOne
    @JoinColumn(name = "debtor_id", nullable = false)
    private User debtor;

    // The person who is OWED money (Destination)
    @ManyToOne
    @JoinColumn(name = "creditor_id", nullable = false)
    private User creditor;

    @ManyToOne
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    public Debt(User debtor, User creditor, Household household, BigDecimal amount) {
        // 1. Fail-Fast Validation, throws NullPointerException if any validation fails
        Objects.requireNonNull(debtor, "Debtor cannot be null");
        Objects.requireNonNull(creditor, "Creditor cannot be null");
        Objects.requireNonNull(household, "Household cannot be null");
        Objects.requireNonNull(amount, "Debt amount cannot be null");

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Debt amount must be strictly greater than zero.");
        }

        if (debtor.equals(creditor)) {
            throw new IllegalArgumentException("Debtor and Creditor cannot be the same user.");
        }

        // 2. Assignment
        this.debtor = debtor;
        this.creditor = creditor;
        this.household = household;
        this.amount = amount;
    }
}