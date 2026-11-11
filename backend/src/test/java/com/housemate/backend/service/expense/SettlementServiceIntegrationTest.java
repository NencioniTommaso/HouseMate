package com.housemate.backend.service.expense;

import com.housemate.backend.model.expense.Debt;
import com.housemate.backend.model.household.Household;
import com.housemate.backend.model.household.HouseholdMembership;
import com.housemate.backend.model.user.User;
import com.housemate.backend.repository.expense.DebtRepository;
import com.housemate.backend.repository.expense.SettlementRepository;
import com.housemate.backend.repository.household.HouseholdRepository;
import com.housemate.backend.repository.user.UserRepository;
import com.housemate.shared.dto.expense.request.SettlementCreateRequestDTO;
import com.housemate.shared.dto.expense.response.SettlementResponseDTO;
import com.housemate.shared.enums.UserTransactionRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(SettlementService.class)
@DisplayName("SettlementService Integration Tests")
@SuppressWarnings("null")
class SettlementServiceIntegrationTest {

    @Autowired
    private SettlementService settlementService;

    @Autowired
    private SettlementRepository settlementRepository;

    @Autowired
    private DebtRepository debtRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HouseholdRepository householdRepository;

    @Test
    @DisplayName("settleDebt persists settlement and reduces debt amount for partial settlement")
    void settleDebt_persistsAndReducesDebt_onPartialSettlement() {
        Household household = persistHousehold("Settlement House");
        User debtor = persistUser("Alice", "Debtor", "alice.settle.it@test.com");
        User creditor = persistUser("Bob", "Creditor", "bob.settle.it@test.com");
        persistMembership(household, debtor, false);
        persistMembership(household, creditor, true);

        Debt debt = debtRepository.saveAndFlush(new Debt(debtor, creditor, household, new BigDecimal("100.00")));

        SettlementCreateRequestDTO request = new SettlementCreateRequestDTO(
                debt.getId(),
                creditor.getId(),
                new BigDecimal("40.00"),
                "bank transfer"
        );

        SettlementResponseDTO result = settlementService.settleDebt(debtor.getId(), request);

        assertThat(result.userTransactionRole()).isEqualTo(UserTransactionRole.DEBTOR);
        assertThat(result.amount()).isEqualByComparingTo("40.00");

        Debt updatedDebt = debtRepository.findById(debt.getId()).orElseThrow();
        assertThat(updatedDebt.getAmount()).isEqualByComparingTo("60.00");

        assertThat(settlementRepository.findAll())
                .hasSize(1)
                .first()
                .satisfies(savedSettlement -> {
                    assertThat(savedSettlement.getDebt().getId()).isEqualTo(debt.getId());
                    assertThat(savedSettlement.getDebtor().getId()).isEqualTo(debtor.getId());
                    assertThat(savedSettlement.getCreditor().getId()).isEqualTo(creditor.getId());
                    assertThat(savedSettlement.getAmount()).isEqualByComparingTo("40.00");
                });
    }

    @Test
    @DisplayName("settleDebt keeps debt row and sets amount to zero on full settlement")
    void settleDebt_setsDebtToZero_onFullSettlement() {
        Household household = persistHousehold("Settlement House");
        User debtor = persistUser("Clara", "Debtor", "clara.settle.it@test.com");
        User creditor = persistUser("Dan", "Creditor", "dan.settle.it@test.com");
        persistMembership(household, debtor, false);
        persistMembership(household, creditor, true);

        Debt debt = debtRepository.saveAndFlush(new Debt(debtor, creditor, household, new BigDecimal("55.00")));

        SettlementCreateRequestDTO request = new SettlementCreateRequestDTO(
                debt.getId(),
                creditor.getId(),
                new BigDecimal("55.00"),
                null
        );

        settlementService.settleDebt(debtor.getId(), request);

        Debt updatedDebt = debtRepository.findById(debt.getId()).orElseThrow();
        assertThat(updatedDebt.getAmount()).isEqualByComparingTo(BigDecimal.ZERO);

        assertThat(settlementRepository.findAll())
                .hasSize(1)
                .first()
                .satisfies(savedSettlement -> {
                    assertThat(savedSettlement.getDebt().getId()).isEqualTo(debt.getId());
                    assertThat(savedSettlement.getAmount()).isEqualByComparingTo("55.00");
                });
    }

    private Household persistHousehold(String name) {
        Household household = new Household();
        household.setName(name);
        return householdRepository.saveAndFlush(household);
    }

    private User persistUser(String name, String surname, String email) {
        User user = new User(name, surname, email, "password");
        return userRepository.saveAndFlush(user);
    }

    private void persistMembership(Household household, User user, boolean isAdmin) {
        HouseholdMembership membership = new HouseholdMembership(household, user, isAdmin);
        user.setHouseholdMembership(membership);
        userRepository.saveAndFlush(user);
    }
}
