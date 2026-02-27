package com.housemate.backend.model.expense;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import com.housemate.backend.model.user.User;

@Entity
@Data
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
    }
}