package com.housemate.shared.dto.chore.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

/**
 * DTO representing the payload required to run queries about an existing Chore.
 */
public record ChoreDeleteRequestDTO(
    @NotBlank(message = "Chore description cannot be blank")
    @Pattern(regexp = "^[a-zA-Z0-9 ]+$", message = "Solo lettere, numeri e spazi consentiti")
    String description,


    @NotNull(message = "Household ID cannot be null")
    UUID householdId
) {}
