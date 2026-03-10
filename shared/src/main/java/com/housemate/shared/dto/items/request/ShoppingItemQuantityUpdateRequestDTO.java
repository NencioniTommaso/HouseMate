package com.housemate.shared.dto.items.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ShoppingItemQuantityUpdateRequestDTO(
    @NotNull(message = "Item quantity cannot be null")
    @NotBlank(message = "Item quantity is required")
    String quantity
) {}
