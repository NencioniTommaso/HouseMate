package com.housemate.shared.dto.chore.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

/**
 * DTO representing the payload required to create a new Chore.
 */
public record ChoreCreateRequestDTO(
        @NotBlank(message = "Chore description cannot be blank")
        @Pattern(regexp = "^[a-zA-Z0-9 ]+$", message = "Solo lettere, numeri e spazi consentiti")
        String description,

        @NotNull(message = "Frequency days cannot be null")
        @Positive(message = "Frequency days must be a positive number")
        Integer frequencyDays,

        @NotNull(message = "Household ID cannot be null")
        UUID householdId
) {}
