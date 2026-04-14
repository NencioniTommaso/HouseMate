package com.housemate.shared.dto.household.response;

import com.housemate.shared.dto.user.response.UserResponseDTO;

/**
 * DTO representing a household member with user data and membership details.
 */
public record HouseholdMemberResponseDTO(
    UserResponseDTO user,
    HouseholdMembershipResponseDTO membership
) {}
