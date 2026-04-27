package com.housemate.backend.service.expense;

import com.housemate.backend.model.expense.Debt;
import com.housemate.backend.model.household.Household;
import com.housemate.backend.model.household.HouseholdMembership;
import com.housemate.backend.model.user.User;
import com.housemate.backend.repository.expense.DebtRepository;
import com.housemate.backend.repository.household.HouseholdRepository;
import com.housemate.backend.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(DebtService.class)
@DisplayName("DebtService Integration Tests")
@SuppressWarnings("null")
class DebtServiceIntegrationTest {

    @Autowired
    private DebtService debtService;

    @Autowired
    private DebtRepository debtRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HouseholdRepository householdRepository;

    @Test
    @DisplayName("addDebt reduces inverse debt when inverse is greater than new amount")
    void addDebt_reducesInverseDebt_whenInverseIsGreater() {
        Household household = persistHousehold("Debt House");
        User debtor = persistUser("Alice", "Debtor", "alice.debtor.it@test.com");
        User creditor = persistUser("Bob", "Creditor", "bob.creditor.it@test.com");
        persistMembership(household, debtor, false);
        persistMembership(household, creditor, true);

        debtRepository.saveAndFlush(new Debt(creditor, debtor, household, new BigDecimal("30.00")));

        debtService.addDebt(debtor.getId(), creditor.getId(), household.getId(), new BigDecimal("20.00"));

        assertThat(debtRepository.findAll())
                .hasSize(1)
                .first()
                .satisfies(remaining -> {
                    assertThat(remaining.getDebtor().getId()).isEqualTo(creditor.getId());
                    assertThat(remaining.getCreditor().getId()).isEqualTo(debtor.getId());
                    assertThat(remaining.getAmount()).isEqualByComparingTo("10.00");
                });
    }

    @Test
    @DisplayName("addDebt keeps inverse debt as zero and creates forward remainder when new amount exceeds inverse")
    void addDebt_keepsInverseAsZeroAndCreatesForwardRemainder_whenNewExceedsInverse() {
        Household household = persistHousehold("Debt House");
        User debtor = persistUser("Clara", "Debtor", "clara.debtor.it@test.com");
        User creditor = persistUser("Dan", "Creditor", "dan.creditor.it@test.com");
        persistMembership(household, debtor, false);
        persistMembership(household, creditor, true);

        debtRepository.saveAndFlush(new Debt(creditor, debtor, household, new BigDecimal("30.00")));

        debtService.addDebt(debtor.getId(), creditor.getId(), household.getId(), new BigDecimal("50.00"));

        List<Debt> debts = debtRepository.findAll().stream()
                .sorted(Comparator.comparing(Debt::getAmount))
                .toList();

        assertThat(debts).hasSize(2);
        assertThat(debts.get(0).getDebtor().getId()).isEqualTo(creditor.getId());
        assertThat(debts.get(0).getCreditor().getId()).isEqualTo(debtor.getId());
        assertThat(debts.get(0).getAmount()).isEqualByComparingTo("0.00");

        assertThat(debts.get(1).getDebtor().getId()).isEqualTo(debtor.getId());
        assertThat(debts.get(1).getCreditor().getId()).isEqualTo(creditor.getId());
        assertThat(debts.get(1).getAmount()).isEqualByComparingTo("20.00");
    }

    @Test
    @DisplayName("addDebt keeps inverse debt entity and sets amount to zero on exact cancellation")
    void addDebt_keepsInverseDebtAndSetsZero_whenExactCancellation() {
        Household household = persistHousehold("Debt House");
        User debtor = persistUser("Eve", "Debtor", "eve.debtor.it@test.com");
        User creditor = persistUser("Finn", "Creditor", "finn.creditor.it@test.com");
        persistMembership(household, debtor, false);
        persistMembership(household, creditor, true);

        debtRepository.saveAndFlush(new Debt(creditor, debtor, household, new BigDecimal("30.00")));

        debtService.addDebt(debtor.getId(), creditor.getId(), household.getId(), new BigDecimal("30.00"));

        assertThat(debtRepository.findAll())
                .hasSize(1)
                .first()
                .satisfies(remaining -> {
                    assertThat(remaining.getDebtor().getId()).isEqualTo(creditor.getId());
                    assertThat(remaining.getCreditor().getId()).isEqualTo(debtor.getId());
                    assertThat(remaining.getAmount()).isEqualByComparingTo("0.00");
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
