package com.housemate.backend.service.expense;

import com.housemate.backend.model.expense.Expense;
import com.housemate.backend.model.expense.ExpenseShare;
import com.housemate.backend.model.user.User;
import com.housemate.backend.model.household.Household;
import com.housemate.backend.repository.expense.ExpenseRepository;
import com.housemate.backend.repository.expense.QuerySpecification;
import com.housemate.backend.repository.user.UserRepository;
import com.housemate.backend.service.expense.strategy.ExpenseSplitStrategy;
import com.housemate.backend.service.expense.strategy.ExpenseSplitStrategyFactory;
import com.housemate.shared.dto.expense.request.ExpenseCreateRequestDTO;
import com.housemate.shared.dto.expense.request.TransactionFilterRequestDTO;
import com.housemate.shared.dto.expense.request.ExpenseShareRequestDTO;
import com.housemate.shared.dto.expense.response.ExpenseResponseDTO;
import com.housemate.shared.dto.expense.response.ExpenseShareResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final DebtService debtService;
    private final ExpenseSplitStrategyFactory strategyFactory;

    @Transactional
    public ExpenseResponseDTO createExpense(
            @NonNull UUID payerId,
            @NonNull ExpenseCreateRequestDTO requestDTO) {
        // 1. Fail-Fast Validation
        Assert.notNull(payerId, "Payer ID must not be null");
        Assert.notNull(requestDTO, "Expense request DTO must not be null");
        
        // 2. Fetch Payer and Household
        User payer = userRepository.findById(payerId)
                .orElseThrow(() -> new IllegalArgumentException("Payer not found with ID: " + payerId));
        
        if (payer.getHouseholdMembership() == null || payer.getHouseholdMembership().getHousehold() == null) {
            throw new IllegalStateException("Payer is not currently a member of any household");
        }

        Household household = payer.getHouseholdMembership().getHousehold();

        // 3. Initialize the Root Expense
        Expense expense = new Expense(
            requestDTO.description(), 
            requestDTO.amount(), 
            payer, 
            household,
            requestDTO.splitType()
        );

        Set<UUID> involvedUserIds = requestDTO.shares().stream()
            .map(ExpenseShareRequestDTO::userId)
            .collect(Collectors.toSet());

        involvedUserIds.add(payerId);

        Map<UUID, User> involvedUsersMap = userRepository.findAllById(involvedUserIds).stream()
            .collect(Collectors.toMap(User::getId, user -> user));

        // 4. Strategy Execution
        ExpenseSplitStrategy strategy = strategyFactory.getStrategy(requestDTO.splitType());
        Map<UUID, BigDecimal> calculatedShares = strategy.calculateShares(requestDTO.amount(), requestDTO.shares());
        
        calculatedShares.forEach((userId, amount) -> {
            User user = involvedUsersMap.get(userId);
            if (user == null) {
                throw new IllegalStateException("Calculated share for unknown user ID: " + userId);
            }
            ExpenseShare share = new ExpenseShare(expense, user, amount);
            expense.getShares().add(share);
        });
        
        // 5. Persist the Graph (Expense + Cascade to ExpenseShares)
        expenseRepository.save(expense);

        // 6. Update Debts
        for (ExpenseShare share : expense.getShares()) {
            if (!share.getUser().equals(payer)) {
                debtService.addDebt(share.getUser().getId(), payerId, household.getId(), share.getAmount());
            }
        }

        // 7. Map to Response DTO
        return convertToResponseDTO(expense);
    }
    
    @Transactional(readOnly = true)
    public List<ExpenseResponseDTO> getFilteredExpenses(
            @NonNull UUID userId,
            @NonNull TransactionFilterRequestDTO filter) {
        // 1. Fail-Fast Validation
        Assert.notNull(userId, "User ID must not be null");
        Assert.notNull(filter, "Filter DTO must not be null");
        
        // 2. Fetch user and extract householdId from their current household if not provided in filter
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        UUID householdId = filter.householdId();
        if (householdId == null) {
            if (user.getHouseholdMembership() != null && user.getHouseholdMembership().getHousehold() != null) {
                householdId = user.getHouseholdMembership().getHousehold().getId();
            }
        }

        // 3. Build the dynamic specification based on the DTO
        Specification<Expense> spec = QuerySpecification.buildExpenseFilter(userId, householdId, filter);
        
        // 4. Execute the query using JpaSpecificationExecutor
        return expenseRepository.findAll(spec).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Convert an Expense entity to an ExpenseResponseDTO.
     */
    private ExpenseResponseDTO convertToResponseDTO(Expense expense) {
        return new ExpenseResponseDTO(
                expense.getId(),
                expense.getDescription(),
                expense.getDate(),
                expense.getAmount(),
                expense.getPayer().getId(),
                getFullName(expense.getPayer()),
                expense.getSplitType(),
                expense.getHousehold().getId(),
                expense.getShares().stream()
                        .map(share -> new ExpenseShareResponseDTO(
                                share.getId(),
                                share.getUser().getId(),
                                getFullName(share.getUser()),
                                share.getAmount()
                        ))
                        .collect(Collectors.toList())
        );
    }

    /*
     * Helper method to get full name from a User.
    */
    private String getFullName(User user) {
        return user.getName() + " " + user.getSurname();
    }
}