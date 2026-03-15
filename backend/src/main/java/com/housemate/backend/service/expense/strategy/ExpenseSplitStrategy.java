package com.housemate.backend.service.expense.strategy;

import com.housemate.shared.dto.expense.request.ExpenseShareRequestDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Strategy interface for calculating expense shares based on different splitting methods.
 * Each implementation handles a specific expense split type.
 */
public interface ExpenseSplitStrategy {
    
    Map<UUID, BigDecimal> calculateShares(BigDecimal amount, List<ExpenseShareRequestDTO> shareRequests);
}