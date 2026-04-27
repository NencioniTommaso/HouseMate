package com.housemate.shared.dto.expense.response;

import java.math.BigDecimal;

public record UserNetOverviewResponseDTO(
        BigDecimal actualCashFlowAmount
) {}