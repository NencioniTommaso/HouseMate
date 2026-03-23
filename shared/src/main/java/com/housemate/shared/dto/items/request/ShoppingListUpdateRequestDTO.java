package com.housemate.shared.dto.items.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ShoppingListUpdateRequestDTO(
        @NotNull(message = "Items bought list cannot be null")
        List<Boolean> boughtItems
) {}
