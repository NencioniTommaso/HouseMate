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
    public SettlementResponseDTO settleDebt(UUID userId, SettlementCreateRequestDTO requestDTO) {
        // fetch debt, debtor and creditor from the database to ensure they exist and to get the full entities for validation
        Debt debt = debtRepository.findById(requestDTO.debtId())
                .orElseThrow(() -> new IllegalArgumentException("Debt not found with ID: " + requestDTO.debtId()));

        User debtor = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        User creditor = userRepository.findById(requestDTO.creditorId())
                .orElseThrow(() -> new IllegalArgumentException("Creditor not found with ID: " + requestDTO.creditorId()));

        // Verify that the provided users match the debt relationship
        if (!debt.getDebtor().equals(debtor)) {
            throw new IllegalArgumentException("Provided debtor does not match the debt record");
        }
        if (!debt.getCreditor().equals(creditor)) {
            throw new IllegalArgumentException("Provided creditor does not match the debt record");
        }

        // Fail-Fast validations
        if (requestDTO.amount().compareTo(debt.getAmount()) > 0) {
            throw new IllegalArgumentException("Cannot settle an amount greater than the existing debt.");
        }

        // Create the settlement record
        Settlement settlement = new Settlement(debt, debtor, creditor, requestDTO.amount(), requestDTO.description());
        settlementRepository.save(settlement);

        // Decrease the debt or delete it if fully paid
        if (requestDTO.amount().compareTo(debt.getAmount()) == 0) {
            debtRepository.delete(debt);
        } else {
            debt.setAmount(debt.getAmount().subtract(requestDTO.amount()));
            debtRepository.save(debt);
        }

        return convertToSettlementResponseDTO(settlement, UserTransactionRole.DEBTOR);
    }

    @Transactional(readOnly = true)
    public List<SettlementResponseDTO> getFilteredSettlements(UUID userId, TransactionFilterRequestDTO filter) {
        // 1. Fetch user and extract householdId from their current household if not provided in filter
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        UUID householdId = filter.householdId();
        if (householdId == null) {
            if (user.getHouseholdMembership() != null && user.getHouseholdMembership().getHousehold() != null) {
                householdId = user.getHouseholdMembership().getHousehold().getId();
            }
        }

        // 2. Build the dynamic specification based on the DTO
        Specification<Settlement> spec = QuerySpecification.buildSettlementFilter(userId, householdId, filter);
        
        // 3. Execute the query and map the results to DTOs
        return settlementRepository.findAll(spec)
                .stream()
                .map(s -> convertToSettlementResponseDTO(s, filter.userTransactionRole()))
                .collect(Collectors.toList());
    }

    private SettlementResponseDTO convertToSettlementResponseDTO(
        Settlement settlement, 
        UserTransactionRole userRole
    ) {
        // Determine the involved party based on the user's role in the transaction, and set the involvedId and involvedName accordingly
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

    private String getFullName(User user) {
            return user.getName() + " " + user.getSurname();
    }
}

 