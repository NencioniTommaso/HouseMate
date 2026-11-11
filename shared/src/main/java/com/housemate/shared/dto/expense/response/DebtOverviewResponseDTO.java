package com.housemate.shared.dto.expense.response;

import java.math.BigDecimal;

public record DebtOverviewResponseDTO(
        BigDecimal totalOwedByMe,
        BigDecimal totalOwedToMe
) {}
