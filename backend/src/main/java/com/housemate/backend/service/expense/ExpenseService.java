package com.housemate.backend.service.expense;

import com.housemate.backend.model.expense.Expense;
import com.housemate.backend.model.expense.ExpenseShare;
import com.housemate.backend.model.user.User;
import com.housemate.backend.model.household.Household;
import com.housemate.backend.repository.expense.ExpenseRepository;
import com.housemate.backend.repository.expense.SettlementRepository;
import com.housemate.backend.repository.expense.QuerySpecification;
import com.housemate.backend.repository.user.UserRepository;
import com.housemate.backend.service.expense.strategy.ExpenseSplitStrategy;
import com.housemate.backend.service.expense.strategy.ExpenseSplitStrategyFactory;
import com.housemate.shared.dto.expense.request.ExpenseCreateRequestDTO;
import com.housemate.shared.dto.expense.request.TransactionFilterRequestDTO;
import com.housemate.shared.dto.expense.request.ExpenseShareRequestDTO;
import com.housemate.shared.dto.expense.response.ExpenseResponseDTO;
import com.housemate.shared.dto.expense.response.ExpenseOverviewResponseDTO;
import com.housemate.shared.dto.expense.response.ExpenseShareResponseDTO;
import com.housemate.shared.dto.expense.response.UserSettlementOverviewResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final SettlementRepository settlementRepository;
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

        // 2. Fetch Payer and safely resolve Household
        User payer = userRepository.findById(payerId)
                .orElseThrow(() -> new IllegalArgumentException("Payer not found with ID: " + payerId));

        Household household = getHouseholdFromUserSafely(payer)
                .orElseThrow(() -> new IllegalStateException("Payer is not currently a member of any household"));

        String description = Objects.requireNonNull(requestDTO.description());
        BigDecimal requestedAmount = Objects.requireNonNull(requestDTO.amount());
        var splitType = Objects.requireNonNull(requestDTO.splitType());
        List<ExpenseShareRequestDTO> shareRequests = Objects.requireNonNull(requestDTO.shares());

        // 3. Initialize the Root Expense
        Expense expense = new Expense(
            description,
            requestedAmount,
            Objects.requireNonNull(payer),
            Objects.requireNonNull(household),
            splitType
        );

        // Explicitly defining mutability using HashSet
        Set<UUID> involvedUserIds = shareRequests.stream()
                .map(ExpenseShareRequestDTO::userId)
                .collect(Collectors.toCollection(HashSet::new));
        //involvedUserIds.add(payerId);     this isn't needed since the payer is not necessarily involved in the shares (e.g., they could be excluded from the split)

        Map<UUID, User> involvedUsersMap = userRepository.findAllById(Objects.requireNonNull(involvedUserIds)).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 4. Strategy Execution
        ExpenseSplitStrategy strategy = strategyFactory.getStrategy(splitType);
        Map<UUID, BigDecimal> calculatedShares = strategy.calculateShares(requestedAmount, shareRequests);

        calculatedShares.forEach((userId, amount) -> {
            User user = involvedUsersMap.get(userId);
            if (user == null) {
                throw new IllegalStateException("Calculated share for unknown user ID: " + userId);
            }
            ExpenseShare share = new ExpenseShare(expense, user, Objects.requireNonNull(amount));
            expense.getShares().add(share);
        });

        // 5. Persist the Graph
        expenseRepository.save(expense);

        // 6. Update Debts (Safely comparing IDs, avoiding Proxy .equals() issues)
        for (ExpenseShare share : expense.getShares()) {
            if (!share.getUser().getId().equals(payerId)) {
                debtService.addDebt(
                        Objects.requireNonNull(share.getUser().getId()),
                        Objects.requireNonNull(payerId),
                        Objects.requireNonNull(household.getId()),
                        Objects.requireNonNull(share.getAmount())
                );
            }
        }

        return convertToResponseDTO(expense);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponseDTO> getFilteredExpenses(
            @NonNull UUID userId,
            @NonNull TransactionFilterRequestDTO filter) {
            
        Assert.notNull(userId, "User ID must not be null");
        Assert.notNull(filter, "Filter DTO must not be null");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // Safely resolve household via Optional chaining to prevent NullPointerExceptions
        UUID householdId = filter.householdId() != null 
            ? filter.householdId() 
            : getHouseholdFromUserSafely(user).map(Household::getId).orElse(null);

        Specification<Expense> spec = QuerySpecification.buildExpenseFilter(userId, householdId, filter);

        return expenseRepository.findAll(spec).stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExpenseOverviewResponseDTO getCurrentMonthExpenseOverview(@NonNull UUID userId) {
        Assert.notNull(userId, "User ID must not be null");

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        UUID householdId = getHouseholdFromUserSafely(user)
            .map(Household::getId)
            .orElseThrow(() -> new IllegalStateException("User is not currently a member of any household"));

        LocalDateTime startOfCurrentMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime startOfNextMonth = startOfCurrentMonth.plusMonths(1);

        BigDecimal totalAmount = expenseRepository.sumAmountByHouseholdIdForDateRange(
            householdId,
            startOfCurrentMonth,
            startOfNextMonth
        );
        long expenseCount = expenseRepository.countByHousehold_IdAndDateGreaterThanEqualAndDateLessThan(
            householdId,
            startOfCurrentMonth,
            startOfNextMonth
        );

        return new ExpenseOverviewResponseDTO(
            Objects.requireNonNullElse(totalAmount, BigDecimal.ZERO),
            expenseCount
        );
    }

        @Transactional(readOnly = true)
        public UserSettlementOverviewResponseDTO getCurrentMonthUserSettlementOverview(@NonNull UUID userId) {
        Assert.notNull(userId, "User ID must not be null");

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        UUID householdId = getHouseholdFromUserSafely(user)
            .map(Household::getId)
            .orElseThrow(() -> new IllegalStateException("User is not currently a member of any household"));

        LocalDateTime startOfCurrentMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime startOfNextMonth = startOfCurrentMonth.plusMonths(1);

        BigDecimal totalSettlementsMade = settlementRepository.sumAmountByDebtorIdAndHouseholdIdForDateRange(
            userId,
            householdId,
            startOfCurrentMonth,
            startOfNextMonth
        );

        return new UserSettlementOverviewResponseDTO(
            Objects.requireNonNullElse(totalSettlementsMade, BigDecimal.ZERO)
        );
        }

    private Optional<Household> getHouseholdFromUserSafely(User user) {
        return Optional.ofNullable(user.getHouseholdMembership())
                .map(membership -> membership.getHousehold());
    }

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
                        .toList()
        );
    }

    private String getFullName(User user) {
        return user.getName() + " " + user.getSurname();
    }
}