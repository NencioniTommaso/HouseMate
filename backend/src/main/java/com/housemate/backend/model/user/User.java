package com.housemate.backend.model.user;


import com.housemate.backend.model.household.HouseholdMembership;
import com.housemate.backend.model.expense.Expense;
import com.housemate.backend.model.expense.ExpenseShare;
import com.housemate.backend.model.expense.Debt;
import com.housemate.backend.model.expense.Settlement;
import com.housemate.backend.model.chore.ChoreAssignment;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 50)
    private String surname;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(length = 27, unique = true)
    private String iban;

    @OneToOne(mappedBy = "user")
    private HouseholdMembership householdMembership;

    @OneToMany(mappedBy = "payer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Expense> expensesPaid;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExpenseShare> expenseShares;

    @OneToMany(mappedBy = "debtor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Debt> debtsOwed;

    @OneToMany(mappedBy = "debtor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Settlement> settlementsMade;

    @OneToMany(mappedBy = "creditor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Settlement> settlementsReceived;

    @OneToMany(mappedBy = "creditor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Debt> debtsLent;

    @OneToMany(mappedBy = "assignedUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChoreAssignment> choreAssignments;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Unavailability> unavailabilities;


    public User(String name, String surname, String email, String password) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
    }
}