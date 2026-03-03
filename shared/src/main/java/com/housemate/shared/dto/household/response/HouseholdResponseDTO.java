package com.housemate.shared.dto.household.response;

import com.housemate.shared.dto.user.response.UserResponseDTO;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTO representing a persisted Household to be displayed on the client.
 */
public record HouseholdResponseDTO(
    UUID id,
    String name,
    LocalDate creationDate,
    List<UserResponseDTO> members
) {}
