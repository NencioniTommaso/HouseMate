package com.housemate.shared.dto.items.request;

import jakarta.validation.constraints.NotNull;

public record ShoppingItemStatusUpdateRequestDTO(
   @NotNull(message = "Item status cannot be null")
   Boolean isBought
) {}
