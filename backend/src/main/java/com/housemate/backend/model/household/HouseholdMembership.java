package com.housemate.backend.model.household;

import com.housemate.backend.model.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Join entity representing the many-to-many association between Household and User.
 * Cardinality: Household (1,N) <-> (0,N) User.
 * The association carries the attributes: is_admin and date.
 *
 * The natural composite PK (household_id, user_id) is used via @EmbeddedId,
 * ensuring a user can only have one membership per household.
 */
@Entity
@Table(name = "household_memberships")
@Getter
@Setter
@NoArgsConstructor
public class HouseholdMembership {

    @EmbeddedId
    private HouseholdMembershipId id;

    // @MapsId ties each FK column to the corresponding field in the composite key
    @MapsId("householdId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household", nullable = false)
    private Household household;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user", nullable = false)
    private User user;

    @Column(name = "is_admin", nullable = false)
    private boolean isAdmin;

    @Column(nullable = false)
    private LocalDate date;
}
