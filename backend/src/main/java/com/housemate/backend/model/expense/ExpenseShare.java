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

    public ExpenseShare(
            @NonNull Expense expense,
            @NonNull User user,
            @NonNull BigDecimal amount) {
        // 1. Fail-Fast Validation
        Assert.notNull(expense, "Expense cannot be null");
        Assert.notNull(user, "User cannot be null");
        Assert.notNull(amount, "Expense share amount cannot be null");
        Assert.isTrue(amount.compareTo(BigDecimal.ZERO) > 0, "Expense share amount must be strictly greater than zero.");

        // 2. Assignment
        this.expense = expense;
        this.user = user;
        this.amount = amount;
    }
}