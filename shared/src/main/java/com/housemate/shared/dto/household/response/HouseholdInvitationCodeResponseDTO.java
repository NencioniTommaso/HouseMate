package com.housemate.shared.dto.household.response;

import java.time.LocalDateTime;

/**
 * DTO representing the household invitation code and its last refresh timestamp.
 */
public record HouseholdInvitationCodeResponseDTO(
    String invitationCode,
    LocalDateTime refreshedAt
) {}
