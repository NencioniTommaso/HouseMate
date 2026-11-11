package com.housemate.backend.model.expense;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.housemate.backend.model.user.User;
import com.housemate.backend.model.household.Household;
import com.housemate.shared.enums.ExpenseSplitType;

@Entity
@Table(name = "expenses")
@Getter
@Setter
@NoArgsConstructor
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "expense_description", nullable = false)
    private String description;

    @Column(name = "expense_date", nullable = false, updatable = false)
    private LocalDateTime date;

    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payer_id", nullable = false)
    private User payer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @Enumerated(EnumType.STRING)
    @Column(name = "split_type", nullable = false)
    private ExpenseSplitType splitType;

    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExpenseShare> shares = new ArrayList<>();

    public Expense(
            @NonNull String description,
            @NonNull BigDecimal amount,
            @NonNull User payer,
            @NonNull Household household,
            @NonNull ExpenseSplitType splitType) {
        // 1. Fail-Fast Validation
        Assert.notNull(description, "Description cannot be null");
        Assert.notNull(amount, "Expense amount cannot be null");
        Assert.notNull(payer, "Payer cannot be null");
        Assert.notNull(household, "Household cannot be null");
        Assert.notNull(splitType, "Split type cannot be null");
        Assert.isTrue(amount.compareTo(BigDecimal.ZERO) > 0, "Expense amount must be strictly greater than zero.");

        // 2. Assignment
        this.description = description;
        this.amount = amount;
        this.payer = payer;
        this.household = household;
        this.splitType = splitType;
        this.date = LocalDateTime.now();
    }
}