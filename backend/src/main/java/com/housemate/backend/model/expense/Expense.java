package com.housemate.backend.model.expense;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.housemate.backend.model.user.User;

@Entity
@Table(name = "expenses")
@Getter
@Setter
@NoArgsConstructor
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String description;
    private LocalDateTime date;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @ManyToOne
    @JoinColumn(name = "payer_id", nullable = false)
    private User payer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseSplitType splitType;

    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExpenseShare> shares = new ArrayList<>();

    public Expense(String description, BigDecimal amount, User payer, ExpenseSplitType splitType) {
        this.description = description;
        this.amount = amount;
        this.payer = payer;
        this.splitType = splitType;
        this.date = LocalDateTime.now(); // Defaulting to current time
        
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Expense amount must be greater than zero.");
        }
        
        if (splitType == null) {
            throw new IllegalArgumentException("Split type cannot be null.");
        }
    }
}