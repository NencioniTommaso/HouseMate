package com.housemate.backend.model.chore;

import com.housemate.backend.model.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "chore_assignments")
@Getter
@Setter
@NoArgsConstructor
public class ChoreAssignment {

    @Id
    @Setter(AccessLevel.NONE)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    //@enumerated is hard-required if the attribute is an enum
    @Enumerated(EnumType.STRING)
    @Column(name = "chore_status", nullable = false)
    private ChoreStatus choreStatus;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    //name =  "this column's name"
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_chore_id", nullable = false)
    private Chore assignedChore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id", nullable = false)
    private User assignedUser;

    public ChoreAssignment (LocalDateTime dueDate, Chore assignedChore, User assignedUser) {
        if(assignedChore == null) {
            throw new IllegalArgumentException("Assigned Chore cannot be null when creating a ChoreAssignment.");
        }

        if(assignedUser == null) {
            throw new IllegalArgumentException("Assigned User cannot be null when creating a ChoreAssignment.");
        }

        this.dueDate = dueDate;
        this.assignedChore = assignedChore;
        this.assignedUser = assignedUser;

        this.choreStatus = ChoreStatus.PENDING;
    }

}
