package com.housemate.backend.service.expense;

import com.housemate.backend.model.expense.Debt;
import com.housemate.backend.model.expense.Settlement;
import com.housemate.backend.model.user.User;
import com.housemate.backend.repository.expense.DebtRepository;
import com.housemate.backend.repository.expense.QuerySpecification;
import com.housemate.backend.repository.expense.SettlementRepository;
import com.housemate.backend.repository.user.UserRepository;
import com.housemate.shared.dto.expense.request.SettlementCreateRequestDTO;
import com.housemate.shared.dto.expense.request.SettlementFilterRequestDTO;
import com.housemate.shared.dto.expense.response.SettlementResponseDTO;
import lombok.RequiredArgsConstructor;

import java.util.List;
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
    public SettlementResponseDTO settleDebt(SettlementCreateRequestDTO requestDTO) {
        // fetch debt, debtor and creditor from the database to ensure they exist and to get the full entities for validation
        Debt debt = debtRepository.findById(requestDTO.debtId())
                .orElseThrow(() -> new IllegalArgumentException("Debt not found with ID: " + requestDTO.debtId()));

        User debtor = userRepository.findById(requestDTO.debtorId())
                .orElseThrow(() -> new IllegalArgumentException("Debtor not found with ID: " + requestDTO.debtorId()));

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
        Settlement settlement = new Settlement(debt, debtor, creditor, requestDTO.amount());
        settlementRepository.save(settlement);

        // Decrease the debt or delete it if fully paid
        if (requestDTO.amount().compareTo(debt.getAmount()) == 0) {
            debtRepository.delete(debt);
        } else {
            debt.setAmount(debt.getAmount().subtract(requestDTO.amount()));
            debtRepository.save(debt);
        }

        return convertToSettlementResponseDTO(settlement);
    }

    @Transactional(readOnly = true)
    public List<SettlementResponseDTO> getFilteredSettlements(SettlementFilterRequestDTO filter) {
        Specification<Settlement> spec = QuerySpecification.buildSettlementFilter(filter);
        
        return settlementRepository.findAll(spec)
                .stream()
                .map(this::convertToSettlementResponseDTO)
                .collect(Collectors.toList());
    }

    private SettlementResponseDTO convertToSettlementResponseDTO(Settlement settlement) {
        return new SettlementResponseDTO(
                settlement.getId(),
                settlement.getDebt().getId(),
                settlement.getDebtor().getId(),
                getFullName(settlement.getDebtor()),
                settlement.getCreditor().getId(),
                getFullName(settlement.getCreditor()),
                settlement.getAmount(),
                settlement.getSettlementDate()
        );
    }

    private String getFullName(User user) {
            return user.getName() + " " + user.getSurname();
    }
}

 