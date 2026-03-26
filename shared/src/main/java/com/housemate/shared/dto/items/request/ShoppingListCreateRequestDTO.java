package com.housemate.shared.dto.items.request;

import com.housemate.shared.utils.types.ListItem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ShoppingListCreateRequestDTO(

        @NotNull(message = "List name cannot be null")
        @NotBlank(message = "An item name is required")
        @Pattern(regexp = "^[a-zA-Z0-9 ]+$", message = "Item name can only contain letters, numbers, and spaces")
        String name,

        @NotNull(message = "List items cannot be null")
        List<ListItem> items,

        @NotNull(message = "Household ID cannot be null")
        UUID householdId,

        @NotNull(message = "Creation date cannot be null")
        LocalDate creationDate

) {}

