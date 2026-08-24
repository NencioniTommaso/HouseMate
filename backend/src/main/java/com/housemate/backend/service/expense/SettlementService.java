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

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Sort;
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
        
        // 2. Fetch debt and the requesting debtor
        Debt debt = debtRepository.findById(Objects.requireNonNull(requestDTO.debtId()))
                .orElseThrow(() -> new IllegalArgumentException("Debt not found with ID: " + requestDTO.debtId()));

        User debtor = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // 3. Fast Validation
        if (!debt.getDebtor().getId().equals(debtor.getId())) {
            throw new IllegalArgumentException("Provided debtor does not match the debt record");
        }
        if (!debt.getCreditor().getId().equals(requestDTO.creditorId())) {
            throw new IllegalArgumentException("Provided creditor does not match the debt record");
        }
        if (requestDTO.amount().compareTo(debt.getAmount()) > 0) {
            throw new IllegalArgumentException("Cannot settle an amount greater than the existing debt.");
        }
        if (requestDTO.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Settlement amount must be strictly greater than zero");
        }

        // 4. Create and save the settlement record
        Settlement settlement = new Settlement(
            Objects.requireNonNull(debt),
            Objects.requireNonNull(debtor),
            Objects.requireNonNull(debt.getCreditor()),
            Objects.requireNonNull(requestDTO.amount()),
            requestDTO.description()
        );
        settlementRepository.save(settlement);

        // (Soft "Delete" / closing debt by putting it to 0).
        if (requestDTO.amount().compareTo(debt.getAmount()) == 0) {
            debt.setAmount(java.math.BigDecimal.ZERO);
        } else {
            debt.setAmount(debt.getAmount().subtract(requestDTO.amount()));
        }
        
        // FIX: Ensure we save the updated debt record after modifying the amount
        debtRepository.save(debt);

        return convertToSettlementResponseDTO(settlement, UserTransactionRole.DEBTOR, userId);
    }

    @Transactional(readOnly = true)
    public List<SettlementResponseDTO> getFilteredSettlements(
            @NonNull UUID userId,
            @NonNull TransactionFilterRequestDTO filter) {
        
        Assert.notNull(userId, "User ID must not be null");
        Assert.notNull(filter, "Filter DTO must not be null");
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        UUID householdId = filter.householdId();
        if (householdId == null) {
            if (user.getHouseholdMembership() != null && user.getHouseholdMembership().getHousehold() != null) {
                householdId = user.getHouseholdMembership().getHousehold().getId();
            }
        }

        Specification<Settlement> spec = QuerySpecification.buildSettlementFilter(userId, householdId, filter);
        
        // Use modern .toList() and pass the requesting userId to the mapper
        return settlementRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "settlementDate"))
                .stream()
                .map(s -> convertToSettlementResponseDTO(Objects.requireNonNull(s), filter.userTransactionRole(), userId))
                .toList();
    }

    private SettlementResponseDTO convertToSettlementResponseDTO(
            @NonNull Settlement settlement,
            UserTransactionRole userRole,
            @NonNull UUID requestingUserId) { // Injected requestingUserId to fix business logic bug
            
        Assert.notNull(settlement, "Settlement must not be null");
        
        UUID involvedId;
        String involvedName;
        
        if (userRole == UserTransactionRole.CREDITOR) {
            involvedId = settlement.getDebtor().getId();
            involvedName = getFullName(Objects.requireNonNull(settlement.getDebtor()));
        } else if (userRole == UserTransactionRole.DEBTOR) {
            involvedId = settlement.getCreditor().getId();
            involvedName = getFullName(Objects.requireNonNull(settlement.getCreditor()));
        } else if (userRole == UserTransactionRole.ALL) {
            // FIX: If fetching ALL, the involved party is whoever the requester is NOT.
            if (settlement.getDebtor().getId().equals(requestingUserId)) {
                userRole = UserTransactionRole.DEBTOR;
                involvedId = settlement.getCreditor().getId();
                involvedName = getFullName(Objects.requireNonNull(settlement.getCreditor()));
            } else {
                userRole = UserTransactionRole.CREDITOR;
                involvedId = settlement.getDebtor().getId();
                involvedName = getFullName(Objects.requireNonNull(settlement.getDebtor()));
            }
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
                settlement.getHousehold().getId() // Again, ensure Settlement entity actually has this
        );
    }

    private String getFullName(@NonNull User user) {
        return user.getName() + " " + user.getSurname();
    }
}