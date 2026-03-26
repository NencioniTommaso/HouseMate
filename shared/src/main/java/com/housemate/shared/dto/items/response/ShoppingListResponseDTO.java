package com.housemate.shared.dto.items.response;

import com.housemate.shared.enums.ShoppingListStatus;
import com.housemate.shared.utils.types.ListItem;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ShoppingListResponseDTO(

        UUID id,
        String name,
        List<ListItem> items,
        ShoppingListStatus status,
        UUID householdId,
        LocalDate creationDate
) {}
