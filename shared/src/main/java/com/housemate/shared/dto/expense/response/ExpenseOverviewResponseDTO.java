package com.housemate.shared.dto.expense.response;

import java.math.BigDecimal;

public record ExpenseOverviewResponseDTO(
        BigDecimal totalAmount,
        Long expenseCount
) {}