package com.housemate.shared.dto.expense;

import java.util.List;
import java.util.UUID;

public record ExpenseCreateDTO(String description, double amount, UUID payerId, UUID householdId, List<UUID> splitAmongUserIds) {}
