package com.housemate.backend.model.expense;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

import com.housemate.backend.model.user.User;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "expense_shares")
public class ExpenseShare {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // The parent Expense this share belongs to
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "expense_id", nullable = false)
    private Expense expense;

    // The User involved
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    public ExpenseShare(Expense expense, User user, BigDecimal amount) {
        // 1. Fail-Fast Validation, throws NullPointerException if any validation fails
        Objects.requireNonNull(expense, "Expense cannot be null");
        Objects.requireNonNull(user, "User cannot be null");
        Objects.requireNonNull(amount, "Expense share amount cannot be null");

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Expense share amount must be strictly greater than zero.");
        }

        // 2. Assignment
        this.expense = expense;
        this.user = user;
        this.amount = amount;
    }
}