package com.housemate.backend.model.user;


import com.housemate.backend.model.chore.ChoreAssignment;
import com.housemate.backend.model.expense.ExpenseShare;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "users") // Usiamo il plurale per la tabella nel database
@Getter
@Setter
@NoArgsConstructor
public class User {

    // --- CHIAVE PRIMARIA ---
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // --- ATTRIBUTI BASE ---
    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 50)
    private String surname;

    // L'email deve essere obbligatoria e UNICA (non possono esserci due account con la stessa email)
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    // L'IBAN può essere nullo (magari l'utente non lo inserisce subito)
    // Lunghezza 27 è lo standard per gli IBAN italiani
    @Column(length = 27)
    private String iban;


    // --- RELAZIONI (Le frecce del tuo schema ER) ---
    // Nota: Le relazioni "mappedBy" indicano che la chiave esterna (Foreign Key)
    // si trova nell'altra tabella. L'utente è il lato "1" della relazione 1 a N.

    // 1. Relazione con i Compiti Assegnati
    // "assignedUser" è il nome della variabile che creerai nella classe ChoreAssignment
    @OneToMany(mappedBy = "assignedUser", cascade = CascadeType.ALL)
    private List<ChoreAssignment> choreAssignments;

    // 2. Relazione con le Indisponibilità
    // "user" è il nome della variabile che creerai nella classe Unavailability
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Unavailability> unavailabilities;

    // 3. Relazione con le Quote di Spesa (Debiti)
    @OneToMany(mappedBy = "debtorUser", cascade = CascadeType.ALL)
    private List<ExpenseShare> expenseShares;

    /* * Puoi aggiungere qui le altre relazioni man mano che crei le entità:
     * - Lista delle Spese Pagate (Expense)
     * - Lista dei Pagamenti Effettuati (Settlement)
     * - Lista delle Appartenenze alle Case (Membership)
     */
}