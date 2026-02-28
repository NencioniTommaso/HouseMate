package com.housemate.backend.model.expense;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    @ManyToOne
    @JoinColumn(name = "debt_id", nullable = false)
    private Debt debt;

    // The person paying the debt
    @ManyToOne
    @JoinColumn(name = "debtor_id", nullable = false)
    private User debtor;

    // The person receiving the payment
    @ManyToOne
    @JoinColumn(name = "creditor_id", nullable = false)
    private User creditor;

    // The amount being paid towards the debt
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    // When the settlement was made
    private LocalDateTime settlementDate;

    public Settlement(Debt debt, User debtor, User creditor, BigDecimal amount) {
        this.debt = debt;
        this.debtor = debtor;
        this.creditor = creditor;
        this.amount = amount;
        this.settlementDate = LocalDateTime.now();

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Settlement amount must be greater than zero.");
        }
    }
}
