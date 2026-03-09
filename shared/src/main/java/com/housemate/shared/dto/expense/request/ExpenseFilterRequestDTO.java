package com.housemate.shared.dto.expense.request;

import java.util.UUID;

import com.housemate.shared.utils.types.DateRange;

public record ExpenseFilterRequestDTO(
    UUID householdId,
    UUID payerId,
    UUID involvedId,
    DateRange dateRange
) {}
