package com.housemate.backend.model.expense;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import com.housemate.backend.model.user.User;

@Entity
@Table(name = "settlements")
@Getter
@Setter
@NoArgsConstructor
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // The Debt being settled
    @ManyToOne(optional = false)
    @JoinColumn(name = "debt_id", nullable = false)
    private Debt debt;

    // The person paying the debt
    @ManyToOne(optional = false)
    @JoinColumn(name = "debtor_id", nullable = false)
    private User debtor;

    // The person receiving the payment
    @ManyToOne(optional = false)
    @JoinColumn(name = "creditor_id", nullable = false)
    private User creditor;

    // The amount being paid towards the debt
    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    // When the settlement was made
    @Column(name = "settlement_date", nullable = false, updatable = false)
    private LocalDateTime settlementDate;

    public Settlement(Debt debt, User debtor, User creditor, BigDecimal amount) {
        // 1. Fail-Fast Validation, throws NullPointerException if any validation fails
        Objects.requireNonNull(debt, "Debt cannot be null");
        Objects.requireNonNull(debtor, "Debtor cannot be null");
        Objects.requireNonNull(creditor, "Creditor cannot be null");
        Objects.requireNonNull(amount, "Settlement amount cannot be null");

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Settlement amount must be strictly greater than zero.");
        }

        if (debtor.equals(creditor)) {
            throw new IllegalArgumentException("Debtor and Creditor cannot be the same user.");
        }

        // 2. Assignment
        this.debt = debt;
        this.debtor = debtor;
        this.creditor = creditor;
        this.amount = amount;
        this.settlementDate = LocalDateTime.now();
    }
}