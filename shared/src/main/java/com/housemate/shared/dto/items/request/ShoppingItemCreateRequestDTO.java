package com.housemate.shared.dto.items.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record ShoppingItemCreateRequestDTO(

        @NotNull(message = "Item name cannot be null")
        @NotBlank(message = "An item name is required")
        @Pattern(regexp = "^[a-zA-Z0-9 ]+$", message = "Item name can only contain letters, numbers, and spaces")
        String name,

        @NotNull(message = "Item quantity cannot be null")
        @NotBlank(message = "Item quantity is required")
        @Pattern(regexp = "^[a-zA-Z0-9 ]+$", message = "Item name can only contain letters, numbers, and spaces")
        String quantity,

        @NotNull(message = "Household ID cannot be null")
        UUID householdId

) {}

