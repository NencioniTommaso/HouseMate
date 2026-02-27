package com.housemate.backend.model.expense;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.UUID;

import com.housemate.backend.model.user.User;

@Entity
@Data
@Table(name = "debts")
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
    }
}