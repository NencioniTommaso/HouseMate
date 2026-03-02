package com.housemate.backend.model.chore;

import com.housemate.backend.model.household.Household;
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

    @Column(name = "description", nullable = false, length = 50)
    private String description;

    @Column(name = "frequency_in_days", nullable = false)
    private Integer frequency;

    @OneToMany(mappedBy = "assignedChore", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChoreAssignment> choreAssignments;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    public Chore(String description, Integer frequency, Household household) {

        if(household == null) {
            throw new IllegalArgumentException("Household cannot be null when creating a Chore.");
        }

        this.description = description;
        this.frequency = frequency;
        this.household = household;

        this.choreAssignments = new ArrayList<>();
    }
}

