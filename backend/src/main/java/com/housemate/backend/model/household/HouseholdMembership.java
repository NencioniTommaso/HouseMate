package com.housemate.backend.model.household;

import com.housemate.backend.model.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

/**
 * Membership entity linking a User to their single Household.
 * The PK is shared with User (via @MapsId on the @OneToOne), which
 * naturally enforces that each user belongs to at most one household.
 */
@Entity
@Table(name = "household_memberships")
@Getter
@Setter
@NoArgsConstructor
public class HouseholdMembership {

    @Id
    private UUID id; // mirrors user.id, set automatically by @MapsId

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @Column(name = "is_admin", nullable = false)
    private boolean isAdmin;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDate date;


    public HouseholdMembership(Household household, User user, boolean isAdmin) {
        if (household == null) {
            throw new IllegalArgumentException("Household cannot be null");
        }
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        this.household = household;
        this.user = user;
        this.isAdmin = isAdmin;
    }
}
