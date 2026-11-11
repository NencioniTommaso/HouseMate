package com.housemate.shared.dto.household.response;

import java.time.LocalDate;

/**
 * DTO representing membership metadata for a user in a household.
 */
public record HouseholdMembershipResponseDTO(
    boolean isAdmin,
    LocalDate date
) {}
