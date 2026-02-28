package com.housemate.backend.model.expense;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

import com.housemate.backend.model.user.User;

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

    private BigDecimal amount = BigDecimal.ZERO;

    public Debt(User debtor, User creditor, BigDecimal amount) {
        this.debtor = debtor;
        this.creditor = creditor;
        this.amount = amount;

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Debt amount must be greater than zero.");
        }
    }
}