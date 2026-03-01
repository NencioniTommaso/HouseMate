package com.housemate.backend.model.household;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class HouseholdMembershipId implements Serializable {

    @Column(name = "household")
    private UUID householdId;

    @Column(name = "user")
    private UUID userId;
}
