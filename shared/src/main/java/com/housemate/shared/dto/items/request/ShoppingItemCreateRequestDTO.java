package com.housemate.shared.dto.items.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ShoppingItemCreateRequestDTO(

        @NotNull(message = "Item name cannot be null")
        @NotBlank(message = "An item name is required")
        String name,

        @NotNull(message = "Item quantity cannot be null")
        @NotBlank(message = "Item quantity is required")
        String quantity,

        @NotNull(message = "Household ID cannot be null")
        UUID householdId

) {}

