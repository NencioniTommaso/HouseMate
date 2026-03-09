package com.housemate.backend.service.expense;

import com.housemate.backend.model.expense.Expense;
import com.housemate.backend.model.expense.ExpenseShare;
import com.housemate.backend.model.user.User;
import com.housemate.backend.model.household.Household;
import com.housemate.backend.repository.expense.ExpenseRepository;
import com.housemate.backend.repository.expense.ExpenseShareRepository;
import com.housemate.backend.repository.expense.QuerySpecification;
import com.housemate.backend.repository.user.UserRepository;
import com.housemate.backend.service.expense.strategy.ExpenseSplitStrategyFactory;
import com.housemate.shared.dto.expense.request.ExpenseCreateRequestDTO;
import com.housemate.shared.dto.expense.request.ExpenseFilterRequestDTO;
import com.housemate.shared.dto.expense.request.ExpenseShareRequestDTO;
import com.housemate.shared.dto.expense.response.ExpenseResponseDTO;
import com.housemate.shared.dto.expense.response.ExpenseShareResponseDTO;
import lombok.RequiredArgsConstructor;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseShareRepository expenseShareRepository;
    private final UserRepository userRepository;
    private final DebtService debtService;
    private final ExpenseSplitStrategyFactory strategyFactory;

    /**
     * Create a new expense from the provided DTO and return the created expense as a DTO.
     * 
     * @param requestDTO the expense creation request DTO
     * @return the created expense as a response DTO
     */
    @Transactional
    public ExpenseResponseDTO createExpense(ExpenseCreateRequestDTO requestDTO) {
        // Fetch the payer user
        User payer = userRepository.findById(requestDTO.payerId())
                .orElseThrow(() -> new IllegalArgumentException("Payer not found with ID: " + requestDTO.payerId()));

        // Fetch the household (use payer's current household)
        if (payer.getHouseholdMembership() == null || payer.getHouseholdMembership().getHousehold() == null) {
            throw new IllegalStateException("Payer is not currently a member of any household");
        }
        Household household = payer.getHouseholdMembership().getHousehold();

        // Extract involved users from the shares
        List<UUID> userIds = requestDTO.shares().stream()
                .map(ExpenseShareRequestDTO::userId)
                .toList();

        List<User> involvedUsers = userRepository.findAllById(userIds);

        if (involvedUsers.size() != userIds.size()) {
            throw new IllegalArgumentException("One or more involved users could not be found");
        }

        // Extract split parameters (amounts) from the shares
        List<BigDecimal> splitParameters = requestDTO.shares()
                .stream()
                .map(com.housemate.shared.dto.expense.request.ExpenseShareRequestDTO::amount)
                .collect(Collectors.toList());

        // 1. Create and persist the root Expense entity
        Expense expense = new Expense(requestDTO.description(), requestDTO.amount(), payer, household, requestDTO.splitType());
        expense = expenseRepository.save(expense);

        // 2. Select the correct strategy dynamically using the factory
        var strategy = strategyFactory.getStrategy(requestDTO.splitType());
        
        // 3. Calculate shares
        List<ExpenseShare> shares = strategy.calculateShares(expense, involvedUsers, splitParameters);
        expenseShareRepository.saveAll(shares);

        // 4. Update the graph of Debts
        for (ExpenseShare share : shares) {
            // The payer doesn't owe themselves
            if (!share.getUser().equals(payer)) {
                debtService.addDebt(share.getUser().getId(), payer.getId(), household.getId(), share.getAmount());
            }
        }

        return convertToResponseDTO(expense);
    }
    
    @Transactional(readOnly = true)
    public List<ExpenseResponseDTO> getFilteredExpenses(ExpenseFilterRequestDTO filter) {
        // Build the dynamic specification based on the DTO
        Specification<Expense> spec = QuerySpecification.buildExpenseFilter(filter);
        
        // Execute the query using JpaSpecificationExecutor
        return expenseRepository.findAll(spec).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Convert an Expense entity to an ExpenseResponseDTO.
     * 
     * @param expense the expense entity
     * @return the response DTO
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

    /**
     * Helper method to get full name from a User.
     * 
     * @param user the user
     * @return the full name (name + " " + surname)
     */
    private String getFullName(User user) {
        return user.getName() + " " + user.getSurname();
    }
}