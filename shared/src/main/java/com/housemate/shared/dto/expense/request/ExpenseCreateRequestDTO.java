package com.housemate.shared.dto.expense.request;

import com.housemate.shared.enums.ExpenseSplitType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTO representing the payload required to create a new Expense.
 */
public record ExpenseCreateRequestDTO(
    @NotBlank(message = "Description cannot be blank")
    String description,

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.01", message = "Amount must be strictly positive")
    BigDecimal amount,

    @NotNull(message = "Split type cannot be null")
    ExpenseSplitType splitType,

    @NotEmpty(message = "Involved users cannot be empty")
    List<ExpenseShareRequestDTO> shares
) {}