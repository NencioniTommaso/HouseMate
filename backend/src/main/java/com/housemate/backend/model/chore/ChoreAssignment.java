package com.housemate.backend.model.chore;

import com.housemate.backend.model.user.User;
import jakarta.persistence.*;
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
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    //@enumerated is hard-required if the attribute is an enum
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChoreStatus choreStatus;

    @Column
    private LocalDateTime dueDate;

    //name =  "this column's name"
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_chore_id")
    private Chore assignedChore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id")
    private User assignedUser;

}
