package com.housemate.backend.service.expense;

import com.housemate.backend.model.expense.Debt;
import com.housemate.backend.model.expense.Settlement;
import com.housemate.backend.model.user.User;
import com.housemate.backend.repository.expense.DebtRepository;
import com.housemate.backend.repository.expense.QuerySpecification;
import com.housemate.backend.repository.expense.SettlementRepository;
import com.housemate.backend.repository.user.UserRepository;
import com.housemate.shared.dto.expense.request.SettlementCreateRequestDTO;
import com.housemate.shared.dto.expense.request.TransactionFilterRequestDTO;
import com.housemate.shared.dto.expense.response.SettlementResponseDTO;
import com.housemate.shared.enums.UserTransactionRole;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final DebtRepository debtRepository;
    private final UserRepository userRepository;

    @Transactional
    public SettlementResponseDTO settleDebt(
            @NonNull UUID userId,
            @NonNull SettlementCreateRequestDTO requestDTO) {
        // 1. Fail-Fast Validation
        Assert.notNull(userId, "User ID must not be null");
        Assert.notNull(requestDTO, "Settlement request DTO must not be null");
        
        // 2. Fetch debt, debtor and creditor from the database to ensure they exist and to get the full entities for validation
        Debt debt = debtRepository.findById(requestDTO.debtId())
                .orElseThrow(() -> new IllegalArgumentException("Debt not found with ID: " + requestDTO.debtId()));

        User debtor = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        User creditor = userRepository.findById(requestDTO.creditorId())
                .orElseThrow(() -> new IllegalArgumentException("Creditor not found with ID: " + requestDTO.creditorId()));

        // 3. Verify that the provided users match the debt relationship
        if (!debt.getDebtor().equals(debtor)) {
            throw new IllegalArgumentException("Provided debtor does not match the debt record");
        }
        if (!debt.getCreditor().equals(creditor)) {
            throw new IllegalArgumentException("Provided creditor does not match the debt record");
        }

        // 4. Fail-Fast validations
        if (requestDTO.amount().compareTo(debt.getAmount()) > 0) {
            throw new IllegalArgumentException("Cannot settle an amount greater than the existing debt.");
        }

        // 5. Create the settlement record
        Settlement settlement = new Settlement(debt, debtor, creditor, requestDTO.amount(), requestDTO.description());
        settlementRepository.save(settlement);

        // 6. Decrease the debt or delete it if fully paid
        if (requestDTO.amount().compareTo(debt.getAmount()) == 0) {
            debtRepository.delete(debt);
        } else {
            debt.setAmount(debt.getAmount().subtract(requestDTO.amount()));
            debtRepository.save(debt);
        }

        return convertToSettlementResponseDTO(settlement, UserTransactionRole.DEBTOR);
    }

    @Transactional(readOnly = true)
    public List<SettlementResponseDTO> getFilteredSettlements(
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
        Specification<Settlement> spec = QuerySpecification.buildSettlementFilter(userId, householdId, filter);
        
        // 4. Execute the query and map the results to DTOs
        return settlementRepository.findAll(spec)
                .stream()
                .map(s -> convertToSettlementResponseDTO(s, filter.userTransactionRole()))
                .collect(Collectors.toList());
    }

    private SettlementResponseDTO convertToSettlementResponseDTO(
            @NonNull Settlement settlement,
            UserTransactionRole userRole) {
        // 1. Fail-Fast Validation
        Assert.notNull(settlement, "Settlement must not be null");
        
        // 2. Determine the involved party based on the user's role in the transaction, and set the involvedId and involvedName accordingly
        UUID involvedId;
        String involvedName;
        
        if (userRole == UserTransactionRole.CREDITOR) {
            involvedId = settlement.getDebtor().getId();
            involvedName = getFullName(settlement.getDebtor());
        } else if (userRole == UserTransactionRole.DEBTOR) {
            involvedId = settlement.getCreditor().getId();
            involvedName = getFullName(settlement.getCreditor());
        } else if (userRole == UserTransactionRole.ALL) {
            // For ALL role, return debtor as the involved party by default
            involvedId = settlement.getDebtor().getId();
            involvedName = getFullName(settlement.getDebtor());
        } else {
            throw new IllegalArgumentException("Invalid user role for settlement response");
        }

        return new SettlementResponseDTO(
                settlement.getId(),
                userRole,
                involvedId,
                involvedName,
                settlement.getAmount(),
                settlement.getSettlementDate(),
                settlement.getDescription(),
                settlement.getHousehold().getId()
        );
    }

    private String getFullName(@NonNull User user) {
        Assert.notNull(user, "User must not be null");
        return user.getName() + " " + user.getSurname();
    }
}

 