package com.housemate.shared.dto.expense.request;

import java.util.UUID;

import com.housemate.shared.utils.types.DateRange;

public record ExpenseFilterRequestDTO(
    UUID householdId,
    UUID payerId,
    UUID involvedId, // For filtering expenses where a specific user is involved (not as payer)
    DateRange dateRange
) {}
