package com.housemate.backend.service.expense;

import com.housemate.backend.model.expense.Debt;
import com.housemate.backend.model.household.Household;
import com.housemate.backend.model.household.HouseholdMembership;
import com.housemate.backend.model.user.User;
import com.housemate.backend.repository.expense.DebtRepository;
import com.housemate.backend.repository.expense.QuerySpecification;
import com.housemate.backend.repository.household.HouseholdRepository;
import com.housemate.backend.repository.user.UserRepository;
import com.housemate.shared.dto.expense.request.DebtFilterRequestDTO;
import com.housemate.shared.dto.expense.response.DebtOverviewResponseDTO;
import com.housemate.shared.dto.expense.response.DebtResponseDTO;
import com.housemate.shared.enums.UserTransactionRole;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"null", "unchecked"})
class DebtServiceTest {

    @Mock
    private DebtRepository debtRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HouseholdRepository householdRepository;

    private DebtService debtService;

    private UUID debtorId;
    private UUID creditorId;
    private UUID householdId;
    private User debtor;
    private User creditor;
    private Household household;

    @BeforeEach
    void setUp() {
        debtService = new DebtService(debtRepository, userRepository, householdRepository);

        debtorId = UUID.randomUUID();
        creditorId = UUID.randomUUID();
        householdId = UUID.randomUUID();

        debtor = createUser(debtorId, "Alice", "Debtor");
        creditor = createUser(creditorId, "Bob", "Creditor");

        household = new Household();
        household.setId(householdId);
        household.setName("Debt Household");

        HouseholdMembership membership = new HouseholdMembership();
        membership.setUser(debtor);
        membership.setHousehold(household);
        debtor.setHouseholdMembership(membership);
    }

    @Nested
    class AddDebtTests {

        @Test
        void addDebt_withValidInputs_createsNewDebt() {
            // Arrange
            BigDecimal amount = new BigDecimal("50.00");

            when(userRepository.findById(debtorId)).thenReturn(Optional.of(debtor));
            when(userRepository.findById(creditorId)).thenReturn(Optional.of(creditor));
            when(householdRepository.findById(householdId)).thenReturn(Optional.of(household));
            when(debtRepository.findByDebtorAndCreditorAndHousehold(creditor, debtor, household)).thenReturn(Optional.empty());
            when(debtRepository.findByDebtorAndCreditorAndHousehold(debtor, creditor, household)).thenReturn(Optional.empty());

            when(debtRepository.save(any(Debt.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            debtService.addDebt(debtorId, creditorId, householdId, amount);

            // Assert
            ArgumentCaptor<Debt> debtCaptor = ArgumentCaptor.forClass(Debt.class);
            verify(debtRepository).save(debtCaptor.capture());
            Debt saved = debtCaptor.getValue();

            assertThat(saved.getDebtor()).isSameAs(debtor);
            assertThat(saved.getCreditor()).isSameAs(creditor);
            assertThat(saved.getHousehold()).isSameAs(household);
            assertThat(saved.getAmount()).isEqualByComparingTo("50.00");

            verify(debtRepository, never()).delete(any(Debt.class));
        }
        
        @Test
        void addDebt_whenDebtorNotInTargetHousehold_throwsIllegalStateException() {
            // Arrange
            User detachedDebtor = createUser(debtorId, "Detached", "User");
            detachedDebtor.setHouseholdMembership(null); // Not in a household
            
            when(userRepository.findById(debtorId)).thenReturn(Optional.of(detachedDebtor));
            when(userRepository.findById(creditorId)).thenReturn(Optional.of(creditor));
            when(householdRepository.findById(householdId)).thenReturn(Optional.of(household));

            // Act & Assert
            assertThatThrownBy(() -> debtService.addDebt(debtorId, creditorId, householdId, new BigDecimal("50.00")))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Debts can only exist in the user's current household.");

            verifyNoInteractions(debtRepository);
        }

        @Test
        void addDebt_whenDebtorEqualsCreditor_shortCircuitsWithNoRepositoryCalls() {
            // Act
            debtService.addDebt(debtorId, debtorId, householdId, new BigDecimal("10.00"));

            // Assert
            verifyNoInteractions(userRepository, householdRepository, debtRepository);
        }

        @Test
        void addDebt_whenInverseDebtIsGreater_reducesInverseDebt() {
            // Arrange
            BigDecimal inverseAmount = new BigDecimal("30.00");
            BigDecimal newAmount = new BigDecimal("20.00");

            when(userRepository.findById(debtorId)).thenReturn(Optional.of(debtor));
            when(userRepository.findById(creditorId)).thenReturn(Optional.of(creditor));
            when(householdRepository.findById(householdId)).thenReturn(Optional.of(household));

            Debt inverseDebt = mock(Debt.class);
            when(inverseDebt.getAmount()).thenReturn(inverseAmount);
            when(debtRepository.findByDebtorAndCreditorAndHousehold(creditor, debtor, household)).thenReturn(Optional.of(inverseDebt));

            // Act
            debtService.addDebt(debtorId, creditorId, householdId, newAmount);

            // Assert
            verify(inverseDebt).setAmount(new BigDecimal("10.00"));
            verify(debtRepository).save(inverseDebt);
            verify(debtRepository, never()).delete(inverseDebt);
            verifyNoMoreInteractions(debtRepository);
        }

        @Test
        void addDebt_whenInverseDebtMatchesExactly_deletesInverseDebt() {
            // Arrange
            BigDecimal amount = new BigDecimal("50.00");

            when(userRepository.findById(debtorId)).thenReturn(Optional.of(debtor));
            when(userRepository.findById(creditorId)).thenReturn(Optional.of(creditor));
            when(householdRepository.findById(householdId)).thenReturn(Optional.of(household));

            Debt inverseDebt = mock(Debt.class);
            when(inverseDebt.getAmount()).thenReturn(amount);
            when(debtRepository.findByDebtorAndCreditorAndHousehold(creditor, debtor, household)).thenReturn(Optional.of(inverseDebt));

            // Act
            debtService.addDebt(debtorId, creditorId, householdId, amount);

            // Assert
            verify(debtRepository).delete(inverseDebt);
            verify(debtRepository, never()).save(any(Debt.class));
            verifyNoMoreInteractions(debtRepository);
        }

        @Test
        void addDebt_whenNewAmountExceedsInverse_createsForwardDebtForRemainder() {
            // Arrange
            when(userRepository.findById(debtorId)).thenReturn(Optional.of(debtor));
            when(userRepository.findById(creditorId)).thenReturn(Optional.of(creditor));
            when(householdRepository.findById(householdId)).thenReturn(Optional.of(household));

            Debt inverseDebt = mock(Debt.class);
            when(inverseDebt.getAmount()).thenReturn(new BigDecimal("30.00"));
            when(debtRepository.findByDebtorAndCreditorAndHousehold(creditor, debtor, household)).thenReturn(Optional.of(inverseDebt));
            when(debtRepository.findByDebtorAndCreditorAndHousehold(debtor, creditor, household)).thenReturn(Optional.empty());
            when(debtRepository.save(any(Debt.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            debtService.addDebt(debtorId, creditorId, householdId, new BigDecimal("50.00"));

            // Assert
            verify(debtRepository).delete(inverseDebt);

            ArgumentCaptor<Debt> debtCaptor = ArgumentCaptor.forClass(Debt.class);
            verify(debtRepository).save(debtCaptor.capture());
            assertThat(debtCaptor.getValue().getAmount()).isEqualByComparingTo("20.00");
        }

        @Test
        void addDebt_whenForwardDebtAlreadyExists_accumulatesAmount() {
            // Arrange
            when(userRepository.findById(debtorId)).thenReturn(Optional.of(debtor));
            when(userRepository.findById(creditorId)).thenReturn(Optional.of(creditor));
            when(householdRepository.findById(householdId)).thenReturn(Optional.of(household));
            when(debtRepository.findByDebtorAndCreditorAndHousehold(creditor, debtor, household)).thenReturn(Optional.empty());

            Debt existing = mock(Debt.class);
            when(existing.getAmount()).thenReturn(new BigDecimal("30.00"));
            when(debtRepository.findByDebtorAndCreditorAndHousehold(debtor, creditor, household)).thenReturn(Optional.of(existing));

            // Act
            debtService.addDebt(debtorId, creditorId, householdId, new BigDecimal("20.00"));

            // Assert
            verify(existing).setAmount(new BigDecimal("50.00"));
            verify(debtRepository).save(existing);
        }

        @Test
        void addDebt_withNullDebtorId_throwsIllegalArgumentException() {
            assertThatThrownBy(() -> debtService.addDebt(null, creditorId, householdId, new BigDecimal("10.00")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Debtor ID must not be null");

            verifyNoInteractions(userRepository, householdRepository, debtRepository);
        }

        @Test
        void addDebt_withNullCreditorId_throwsIllegalArgumentException() {
            assertThatThrownBy(() -> debtService.addDebt(debtorId, null, householdId, new BigDecimal("10.00")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Creditor ID must not be null");

            verifyNoInteractions(userRepository, householdRepository, debtRepository);
        }

        @Test
        void addDebt_withNullHouseholdId_throwsIllegalArgumentException() {
            assertThatThrownBy(() -> debtService.addDebt(debtorId, creditorId, null, new BigDecimal("10.00")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Household ID must not be null");

            verifyNoInteractions(userRepository, householdRepository, debtRepository);
        }

        @Test
        void addDebt_withNullAmount_throwsIllegalArgumentException() {
            assertThatThrownBy(() -> debtService.addDebt(debtorId, creditorId, householdId, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Amount must not be null");

            verifyNoInteractions(userRepository, householdRepository, debtRepository);
        }

        @Test
        void addDebt_whenDebtorNotFound_throwsIllegalArgumentException() {
            when(userRepository.findById(debtorId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> debtService.addDebt(debtorId, creditorId, householdId, new BigDecimal("10.00")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Debtor not found with ID");

            verifyNoInteractions(householdRepository, debtRepository);
        }

        @Test
        void addDebt_whenCreditorNotFound_throwsIllegalArgumentException() {
            when(userRepository.findById(debtorId)).thenReturn(Optional.of(debtor));
            when(userRepository.findById(creditorId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> debtService.addDebt(debtorId, creditorId, householdId, new BigDecimal("10.00")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Creditor not found with ID");

            verifyNoInteractions(householdRepository, debtRepository);
        }

        @Test
        void addDebt_whenHouseholdNotFound_throwsIllegalArgumentException() {
            when(userRepository.findById(debtorId)).thenReturn(Optional.of(debtor));
            when(userRepository.findById(creditorId)).thenReturn(Optional.of(creditor));
            when(householdRepository.findById(householdId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> debtService.addDebt(debtorId, creditorId, householdId, new BigDecimal("10.00")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Household not found with ID");

            verifyNoInteractions(debtRepository);
        }
    }

    @Nested
    class GetFilteredDebtsTests {

        @Test
        void getFilteredDebts_withNullUserId_throwsIllegalArgumentException() {
            DebtFilterRequestDTO filter = new DebtFilterRequestDTO(UserTransactionRole.ALL, null);

            assertThatThrownBy(() -> debtService.getFilteredDebts(null, filter))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User ID must not be null");

            verifyNoInteractions(userRepository, debtRepository);
        }

        @Test
        void getFilteredDebts_withNullFilter_throwsIllegalArgumentException() {
            assertThatThrownBy(() -> debtService.getFilteredDebts(debtorId, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Filter DTO must not be null");

            verifyNoInteractions(userRepository, debtRepository);
        }

        @Test
        void getFilteredDebts_userNotFound_throwsIllegalArgumentException() {
            DebtFilterRequestDTO filter = new DebtFilterRequestDTO(UserTransactionRole.ALL, null);
            when(userRepository.findById(debtorId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> debtService.getFilteredDebts(debtorId, filter))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User not found with ID");

            verify(debtRepository, never()).findAll(any(Specification.class));
        }

        @Test
        void getFilteredDebts_usesUserCurrentHouseholdAndIgnoresFilterHouseholdId() {
            // Arrange
            // Even if the filter requests a random household, the service must override it with the user's current household
            UUID randomHouseholdId = UUID.randomUUID();
            DebtFilterRequestDTO filter = new DebtFilterRequestDTO(UserTransactionRole.ALL, randomHouseholdId);
            
            when(userRepository.findById(debtorId)).thenReturn(Optional.of(debtor));

            Specification<Debt> spec = mock(Specification.class);

            try (MockedStatic<QuerySpecification> querySpec = mockStatic(QuerySpecification.class)) {
                // Must explicitly pass the `householdId` of the user, NOT the random one from the filter
                querySpec.when(() -> QuerySpecification.buildDebtFilter(eq(debtorId), eq(householdId), same(filter)))
                        .thenReturn(spec);
                when(debtRepository.findAll(spec)).thenReturn(Collections.emptyList());

                List<DebtResponseDTO> result = debtService.getFilteredDebts(debtorId, filter);

                assertThat(result).isEmpty();
                querySpec.verify(() -> QuerySpecification.buildDebtFilter(eq(debtorId), eq(householdId), same(filter)));
                verify(debtRepository).findAll(spec);
            }
        }

        @Test
        void getFilteredDebts_whenUserHasNoHousehold_throwsIllegalStateException() {
            // Arrange
            DebtFilterRequestDTO filter = new DebtFilterRequestDTO(UserTransactionRole.ALL, null);
            User userWithoutMembership = createUser(debtorId, "Detached", "User");
            userWithoutMembership.setHouseholdMembership(null); // Invalid state based on app routing
            
            when(userRepository.findById(debtorId)).thenReturn(Optional.of(userWithoutMembership));

            // Act & Assert
            assertThatThrownBy(() -> debtService.getFilteredDebts(debtorId, filter))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("User must be in an active household to view debts.");
            
            // Verify that the database query is blocked
            verifyNoInteractions(debtRepository); 
        }

        @Test
        void getFilteredDebts_mapsDebtToDebtorViewResponse() {
            DebtFilterRequestDTO filter = new DebtFilterRequestDTO(UserTransactionRole.ALL, null);
            when(userRepository.findById(debtorId)).thenReturn(Optional.of(debtor));

            Debt debt = new Debt(debtor, creditor, household, new BigDecimal("25.00"));
            debt.setId(UUID.randomUUID());

            Specification<Debt> spec = mock(Specification.class);

            try (MockedStatic<QuerySpecification> querySpec = mockStatic(QuerySpecification.class)) {
                querySpec.when(() -> QuerySpecification.buildDebtFilter(eq(debtorId), eq(householdId), same(filter)))
                        .thenReturn(spec);
                when(debtRepository.findAll(spec)).thenReturn(List.of(debt));

                List<DebtResponseDTO> result = debtService.getFilteredDebts(debtorId, filter);

                assertThat(result).hasSize(1);
                DebtResponseDTO dto = result.get(0);
                assertThat(dto.debtId()).isEqualTo(debt.getId());
                assertThat(dto.userTransactionRole()).isEqualTo(UserTransactionRole.DEBTOR);
                assertThat(dto.involvedId()).isEqualTo(creditorId);
                assertThat(dto.involvedName()).isEqualTo("Bob Creditor");
                assertThat(dto.amount()).isEqualByComparingTo("25.00");
            }
        }

        @Test
        void getFilteredDebts_mapsDebtToCreditorViewResponse() {
            DebtFilterRequestDTO filter = new DebtFilterRequestDTO(UserTransactionRole.ALL, null);
            when(userRepository.findById(creditorId)).thenReturn(Optional.of(creditor));
            creditor.setHouseholdMembership(debtor.getHouseholdMembership());

            Debt debt = new Debt(debtor, creditor, household, new BigDecimal("25.00"));
            debt.setId(UUID.randomUUID());

            Specification<Debt> spec = mock(Specification.class);

            try (MockedStatic<QuerySpecification> querySpec = mockStatic(QuerySpecification.class)) {
                querySpec.when(() -> QuerySpecification.buildDebtFilter(eq(creditorId), eq(householdId), same(filter)))
                        .thenReturn(spec);
                when(debtRepository.findAll(spec)).thenReturn(List.of(debt));

                List<DebtResponseDTO> result = debtService.getFilteredDebts(creditorId, filter);

                assertThat(result).hasSize(1);
                DebtResponseDTO dto = result.get(0);
                assertThat(dto.userTransactionRole()).isEqualTo(UserTransactionRole.CREDITOR);
                assertThat(dto.involvedId()).isEqualTo(debtorId);
                assertThat(dto.involvedName()).isEqualTo("Alice Debtor");
            }
        }

        @Test
        void getFilteredDebts_whenUserNotParticipantInDebt_throwsIllegalArgumentException() {
            DebtFilterRequestDTO filter = new DebtFilterRequestDTO(UserTransactionRole.ALL, null);
            UUID outsiderId = UUID.randomUUID();
            User outsider = createUser(outsiderId, "Out", "Sider");
            outsider.setHouseholdMembership(debtor.getHouseholdMembership());
            when(userRepository.findById(outsiderId)).thenReturn(Optional.of(outsider));

            Debt debt = new Debt(debtor, creditor, household, new BigDecimal("25.00"));
            debt.setId(UUID.randomUUID());

            Specification<Debt> spec = mock(Specification.class);

            try (MockedStatic<QuerySpecification> querySpec = mockStatic(QuerySpecification.class)) {
                querySpec.when(() -> QuerySpecification.buildDebtFilter(eq(outsiderId), eq(householdId), same(filter)))
                        .thenReturn(spec);
                when(debtRepository.findAll(spec)).thenReturn(List.of(debt));

                assertThatThrownBy(() -> debtService.getFilteredDebts(outsiderId, filter))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("User is neither debtor nor creditor in this debt");
            }
        }
    }

    @Nested
    class DeleteDebtTests {

        @Test
        void deleteDebt_withValidDebtId_deletesDebt() {
            UUID debtId = UUID.randomUUID();
            Debt debt = new Debt(debtor, creditor, household, new BigDecimal("12.00"));
            when(debtRepository.findById(debtId)).thenReturn(Optional.of(debt));

            debtService.deleteDebt(debtId);

            verify(debtRepository).delete(debt);
        }

        @Test
        void deleteDebt_withNullDebtId_throwsIllegalArgumentException() {
            assertThatThrownBy(() -> debtService.deleteDebt(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Debt ID must not be null");

            verifyNoInteractions(debtRepository);
        }

        @Test
        void deleteDebt_debtNotFound_throwsIllegalArgumentException() {
            UUID debtId = UUID.randomUUID();
            when(debtRepository.findById(debtId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> debtService.deleteDebt(debtId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Debt not found with ID");

            verify(debtRepository, never()).delete(any(Debt.class));
        }
    }

    @Nested
    class GetCurrentUserDebtOverviewTests {

        @Test
        void getCurrentUserDebtOverview_withNullUserId_throwsIllegalArgumentException() {
            assertThatThrownBy(() -> debtService.getCurrentUserDebtOverview(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User ID must not be null");

            verifyNoInteractions(userRepository, debtRepository);
        }

        @Test
        void getCurrentUserDebtOverview_userNotFound_throwsIllegalArgumentException() {
            when(userRepository.findById(debtorId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> debtService.getCurrentUserDebtOverview(debtorId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User not found with ID");

            verify(debtRepository, never()).sumAmountByDebtorIdAndHouseholdId(any(UUID.class), any(UUID.class));
            verify(debtRepository, never()).sumAmountByCreditorIdAndHouseholdId(any(UUID.class), any(UUID.class));
        }

        @Test
        void getCurrentUserDebtOverview_whenUserHasNoHousehold_throwsIllegalStateException() {
            User userWithoutMembership = createUser(debtorId, "Detached", "User");
            userWithoutMembership.setHouseholdMembership(null);
            when(userRepository.findById(debtorId)).thenReturn(Optional.of(userWithoutMembership));

            assertThatThrownBy(() -> debtService.getCurrentUserDebtOverview(debtorId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("User must be in an active household to view debts.");

            verify(debtRepository, never()).sumAmountByDebtorIdAndHouseholdId(any(UUID.class), any(UUID.class));
            verify(debtRepository, never()).sumAmountByCreditorIdAndHouseholdId(any(UUID.class), any(UUID.class));
        }

        @Test
        void getCurrentUserDebtOverview_returnsAggregatedValues() {
            when(userRepository.findById(debtorId)).thenReturn(Optional.of(debtor));
            when(debtRepository.sumAmountByDebtorIdAndHouseholdId(debtorId, householdId))
                    .thenReturn(new BigDecimal("135.75"));
            when(debtRepository.sumAmountByCreditorIdAndHouseholdId(debtorId, householdId))
                    .thenReturn(new BigDecimal("40.25"));

            DebtOverviewResponseDTO result = debtService.getCurrentUserDebtOverview(debtorId);

            assertThat(result.totalOwedByMe()).isEqualByComparingTo("135.75");
            assertThat(result.totalOwedToMe()).isEqualByComparingTo("40.25");
            verify(debtRepository).sumAmountByDebtorIdAndHouseholdId(debtorId, householdId);
            verify(debtRepository).sumAmountByCreditorIdAndHouseholdId(debtorId, householdId);
        }

        @Test
        void getCurrentUserDebtOverview_whenSumsAreNull_returnsZeroValues() {
            when(userRepository.findById(debtorId)).thenReturn(Optional.of(debtor));
            when(debtRepository.sumAmountByDebtorIdAndHouseholdId(debtorId, householdId)).thenReturn(null);
            when(debtRepository.sumAmountByCreditorIdAndHouseholdId(debtorId, householdId)).thenReturn(null);

            DebtOverviewResponseDTO result = debtService.getCurrentUserDebtOverview(debtorId);

            assertThat(result.totalOwedByMe()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.totalOwedToMe()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    private User createUser(UUID id, String name, String surname) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setSurname(surname);
        user.setEmail(name.toLowerCase() + "@test.com");
        user.setPassword("password");
        return user;
    }
}