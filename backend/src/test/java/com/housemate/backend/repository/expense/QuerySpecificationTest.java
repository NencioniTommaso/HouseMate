package com.housemate.backend.repository.expense;

import com.housemate.shared.dto.expense.request.DebtFilterRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

@DisplayName("QuerySpecification Unit Tests")
@SuppressWarnings("null")
class QuerySpecificationTest {

    @Test
    @DisplayName("buildDebtFilter should fail fast when userId is null")
    void buildDebtFilter_shouldThrowIllegalArgumentException_whenUserIdIsNull() {
        // Arrange
        UUID userId = null;
        UUID householdId = UUID.randomUUID();
        DebtFilterRequestDTO filter = mock(DebtFilterRequestDTO.class);

        // Act + Assert
        assertThatThrownBy(() -> QuerySpecification.buildDebtFilter(userId, householdId, filter))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User ID must not be null");
    }
}
