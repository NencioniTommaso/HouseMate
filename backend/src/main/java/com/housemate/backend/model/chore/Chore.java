package com.housemate.backend.model.chore;

import com.housemate.backend.model.Household;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "chores")
@Getter
@Setter
@NoArgsConstructor
public class Chore {

    @Id
    @Setter(AccessLevel.NONE)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String description;

    @Column
    private Integer frequency;

    @OneToMany(mappedBy = "assignedChore", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChoreAssignment> choreAssignments;

    @ManyToOne(nullable = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "households_id", nullable = false)
    private Household household;

    public Chore(String description, Integer frequency, Household household) {
        this.description = description;
        this.frequency = frequency;
        this.household = household;

        this.choreAssignments = new ArrayList<>();
    }
}

