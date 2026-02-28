package com.housemate.backend.model.expense;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import com.housemate.backend.model.user.User;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "expense_shares")
public class ExpenseShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The parent Expense (Composition)
    @ManyToOne
    @JoinColumn(name = "expense_id", nullable = false)
    private Expense expense;

    // The User involved
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // The specific share (e.g., 60.00 or 40.00)
    private BigDecimal amount;

    public ExpenseShare(Expense expense, User user, BigDecimal amount) {
        this.expense = expense;
        this.user = user;
        this.amount = amount;

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Expense share amount must be greater than zero.");
        }
    }
}