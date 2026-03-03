package com.housemate.shared.dto.chore.response;

import java.util.UUID;

/**
 * DTO representing a persisted Chore to be displayed on the client.
 */
public record ChoreResponseDTO(
    UUID id,
    String description,
    Integer frequencyDays
) {}
