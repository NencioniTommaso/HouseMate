package com.housemate.shared.dto.household.request;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO representing the payload required to create a new Household.
 */
public record HouseholdCreateRequestDTO(
    @NotBlank(message = "Household name cannot be blank")
    String name
) {}
