package com.housemate.backend.model.expense;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.housemate.backend.model.user.User;
import com.housemate.backend.model.household.Household;

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
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "debt_id", nullable = false)
    private Debt debt;

    // The person paying the debt
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
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

    // Description for the settlement (e.g., payment method, notes)
    @Column(name = "description", length = 500)
    private String description;

    // The household where this settlement occurred (denormalized from Debt for query efficiency)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    public Settlement(
            @NonNull Debt debt,
            @NonNull User debtor,
            @NonNull User creditor,
            @NonNull BigDecimal amount,
            String description) {
        // 1. Fail-Fast Validation
        Assert.notNull(debt, "Debt cannot be null");
        Assert.notNull(debtor, "Debtor cannot be null");
        Assert.notNull(creditor, "Creditor cannot be null");
        Assert.notNull(amount, "Settlement amount cannot be null");
        Assert.isTrue(amount.compareTo(BigDecimal.ZERO) > 0, "Settlement amount must be strictly greater than zero.");
        Assert.isTrue(!debtor.equals(creditor), "Debtor and Creditor cannot be the same user.");

        // 2. Assignment
        this.debt = debt;
        this.debtor = debtor;
        this.creditor = creditor;
        this.amount = amount;
        this.description = description;
        this.household = debt.getHousehold();
        this.settlementDate = LocalDateTime.now();
    }
}