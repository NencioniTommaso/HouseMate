package com.housemate.backend.model.expense;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.math.BigDecimal;
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
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "debtor_id", nullable = false)
    private User debtor;

    // The person who is OWED money (Destination)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creditor_id", nullable = false)
    private User creditor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    public Debt(
            @NonNull User debtor,
            @NonNull User creditor,
            @NonNull Household household,
            @NonNull BigDecimal amount) {
        // 1. Fail-Fast Validation
        Assert.notNull(debtor, "Debtor cannot be null");
        Assert.notNull(creditor, "Creditor cannot be null");
        Assert.notNull(household, "Household cannot be null");
        Assert.notNull(amount, "Debt amount cannot be null");
        Assert.isTrue(amount.compareTo(BigDecimal.ZERO) > 0, "Debt amount must be strictly greater than zero.");
        Assert.isTrue(!debtor.equals(creditor), "Debtor and Creditor cannot be the same user.");

        // 2. Assignment
        this.debtor = debtor;
        this.creditor = creditor;
        this.household = household;
        this.amount = amount;
    }
}