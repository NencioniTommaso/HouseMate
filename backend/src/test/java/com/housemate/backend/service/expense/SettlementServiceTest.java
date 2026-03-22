package com.housemate.backend.service.expense;

import com.housemate.backend.model.expense.Debt;
import com.housemate.backend.model.expense.Settlement;
import com.housemate.backend.model.household.Household;
import com.housemate.backend.model.household.HouseholdMembership;
import com.housemate.backend.model.user.User;
import com.housemate.backend.repository.expense.DebtRepository;
import com.housemate.backend.repository.expense.QuerySpecification;
import com.housemate.backend.repository.expense.SettlementRepository;
import com.housemate.backend.repository.user.UserRepository;
import com.housemate.shared.dto.expense.request.SettlementCreateRequestDTO;
import com.housemate.shared.dto.expense.request.TransactionFilterRequestDTO;
import com.housemate.shared.dto.expense.response.SettlementResponseDTO;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {

    @Mock
    private SettlementRepository settlementRepository;

    @Mock
    private DebtRepository debtRepository;

    @Mock
    private UserRepository userRepository;

    private SettlementService settlementService;

    private UUID debtorId;
    private UUID creditorId;
    private UUID debtId;
    private UUID householdId;
    private User debtor;
    private User creditor;
    private Debt debt;
    private Household household;

    @BeforeEach
    void setUp() {
        settlementService = new SettlementService(settlementRepository, debtRepository, userRepository);

        debtorId = UUID.randomUUID();
        creditorId = UUID.randomUUID();
        debtId = UUID.randomUUID();
        householdId = UUID.randomUUID();

        debtor = createUser(debtorId, "Alice", "Debtor");
        creditor = createUser(creditorId, "Bob", "Creditor");

        household = new Household();
        household.setId(householdId);
        household.setName("Settlement Household");

        HouseholdMembership membership = new HouseholdMembership();
        membership.setUser(debtor);
        membership.setHousehold(household);
        debtor.setHouseholdMembership(membership);

        debt = new Debt(debtor, creditor, household, new BigDecimal("100.00"));
        debt.setId(debtId); 
    }

    @Nested
    class SettleDebtTests {

        @Test
        void settleDebt_withPartialSettlement_createsSettlementAndReducesDebt() {
            // Arrange
            SettlementCreateRequestDTO requestDTO = new SettlementCreateRequestDTO(
                    debtId,
                    creditorId,
                    new BigDecimal("40.00"),
                    "bank transfer"
            );

            when(debtRepository.findById(debtId)).thenReturn(Optional.of(debt));
            when(userRepository.findById(debtorId)).thenReturn(Optional.of(debtor));
            when(settlementRepository.save(any(Settlement.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            SettlementResponseDTO result = settlementService.settleDebt(debtorId, requestDTO);

            // Assert
            ArgumentCaptor<Settlement> settlementCaptor = ArgumentCaptor.forClass(Settlement.class);
            verify(settlementRepository).save(settlementCaptor.capture());
            Settlement saved = settlementCaptor.getValue();

            assertThat(saved.getDebt()).isSameAs(debt);
            assertThat(saved.getDebtor()).isSameAs(debtor);
            assertThat(saved.getCreditor()).isSameAs(creditor);
            assertThat(saved.getAmount()).isEqualByComparingTo("40.00");
            assertThat(saved.getDescription()).isEqualTo("bank transfer");

            verify(debtRepository).save(debt);
            verify(debtRepository, never()).delete(debt);
            assertThat(debt.getAmount()).isEqualByComparingTo("60.00");

            assertThat(result.userTransactionRole()).isEqualTo(UserTransactionRole.DEBTOR);
            assertThat(result.involvedId()).isEqualTo(creditorId);
            assertThat(result.involvedName()).isEqualTo("Bob Creditor");
            assertThat(result.amount()).isEqualByComparingTo("40.00");
            assertThat(result.description()).isEqualTo("bank transfer");
        }

        @Test
        void settleDebt_withFullSettlement_createsSettlementAndDeletesDebt() {
            // Arrange
            SettlementCreateRequestDTO requestDTO = new SettlementCreateRequestDTO(
                    debtId,
                    creditorId,
                    new BigDecimal("100.00"),
                    null
            );

            when(debtRepository.findById(debtId)).thenReturn(Optional.of(debt));
            when(userRepository.findById(debtorId)).thenReturn(Optional.of(debtor));
            when(settlementRepository.save(any(Settlement.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            SettlementResponseDTO result = settlementService.settleDebt(debtorId, requestDTO);

            // Assert
            verify(settlementRepository).save(any(Settlement.class));
            verify(debtRepository).delete(debt);
            verify(debtRepository, never()).save(any(Debt.class));
            assertThat(result.amount()).isEqualByComparingTo("100.00");
        }

        @Test
        void settleDebt_whenSettlementRepositoryFails_doesNotMutateDebtState() {
            // Arrange
            SettlementCreateRequestDTO requestDTO = new SettlementCreateRequestDTO(
                    debtId,
                    creditorId,
                    new BigDecimal("25.00"),
                    "card"
            );

            when(debtRepository.findById(debtId)).thenReturn(Optional.of(debt));
            when(userRepository.findById(debtorId)).thenReturn(Optional.of(debtor));
            when(settlementRepository.save(any(Settlement.class))).thenThrow(new RuntimeException("db failure"));

            // Act & Assert
            assertThatThrownBy(() -> settlementService.settleDebt(debtorId, requestDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("db failure");

            verify(debtRepository, never()).save(any(Debt.class));
            verify(debtRepository, never()).delete(any(Debt.class));
            assertThat(debt.getAmount()).isEqualByComparingTo("100.00");
        }

        @Test
        void settleDebt_withNullUserId_throwsIllegalArgumentException() {
            SettlementCreateRequestDTO requestDTO = new SettlementCreateRequestDTO(
                    debtId,
                    creditorId,
                    new BigDecimal("10.00"),
                    null
            );

            assertThatThrownBy(() -> settlementService.settleDebt(null, requestDTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User ID must not be null");

            verifyNoInteractions(debtRepository, userRepository, settlementRepository);
        }

        @Test
        void settleDebt_withNullRequest_throwsIllegalArgumentException() {
            assertThatThrownBy(() -> settlementService.settleDebt(debtorId, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Settlement request DTO must not be null");

            verifyNoInteractions(debtRepository, userRepository, settlementRepository);
        }

        @Test
        void settleDebt_whenDebtNotFound_throwsIllegalArgumentException() {
            SettlementCreateRequestDTO requestDTO = new SettlementCreateRequestDTO(
                    debtId,
                    creditorId,
                    new BigDecimal("10.00"),
                    null
            );
            when(debtRepository.findById(debtId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> settlementService.settleDebt(debtorId, requestDTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Debt not found with ID");

            verifyNoInteractions(userRepository, settlementRepository);
        }

        @Test
        void settleDebt_whenDebtorUserNotFound_throwsIllegalArgumentException() {
            SettlementCreateRequestDTO requestDTO = new SettlementCreateRequestDTO(
                    debtId,
                    creditorId,
                    new BigDecimal("10.00"),
                    null
            );

            when(debtRepository.findById(debtId)).thenReturn(Optional.of(debt));
            when(userRepository.findById(debtorId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> settlementService.settleDebt(debtorId, requestDTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User not found with ID");

            verify(settlementRepository, never()).save(any(Settlement.class));
            verify(debtRepository, never()).save(any(Debt.class));
            verify(debtRepository, never()).delete(any(Debt.class));
        }

        @Test
        void settleDebt_whenProvidedDebtorDoesNotMatchDebt_throwsIllegalArgumentException() {
            UUID anotherUserId = UUID.randomUUID();
            User anotherUser = createUser(anotherUserId, "Chris", "Other");

            SettlementCreateRequestDTO requestDTO = new SettlementCreateRequestDTO(
                    debtId,
                    creditorId,
                    new BigDecimal("10.00"),
                    null
            );

            when(debtRepository.findById(debtId)).thenReturn(Optional.of(debt));
            when(userRepository.findById(anotherUserId)).thenReturn(Optional.of(anotherUser));

            assertThatThrownBy(() -> settlementService.settleDebt(anotherUserId, requestDTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Provided debtor does not match the debt record");

            verify(settlementRepository, never()).save(any(Settlement.class));
            verify(debtRepository, never()).save(any(Debt.class));
            verify(debtRepository, never()).delete(any(Debt.class));
        }

        @Test
        void settleDebt_whenProvidedCreditorDoesNotMatchDebt_throwsIllegalArgumentException() {
            UUID anotherCreditorId = UUID.randomUUID();

            SettlementCreateRequestDTO requestDTO = new SettlementCreateRequestDTO(
                    debtId,
                    anotherCreditorId,
                    new BigDecimal("10.00"),
                    null
            );

            when(debtRepository.findById(debtId)).thenReturn(Optional.of(debt));
            when(userRepository.findById(debtorId)).thenReturn(Optional.of(debtor));

            assertThatThrownBy(() -> settlementService.settleDebt(debtorId, requestDTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Provided creditor does not match the debt record");

            verify(settlementRepository, never()).save(any(Settlement.class));
        }

        @Test
        void settleDebt_whenAmountExceedsDebt_throwsIllegalArgumentException() {
            SettlementCreateRequestDTO requestDTO = new SettlementCreateRequestDTO(
                    debtId,
                    creditorId,
                    new BigDecimal("101.00"),
                    null
            );

            when(debtRepository.findById(debtId)).thenReturn(Optional.of(debt));
            when(userRepository.findById(debtorId)).thenReturn(Optional.of(debtor));

            assertThatThrownBy(() -> settlementService.settleDebt(debtorId, requestDTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Cannot settle an amount greater than the existing debt");

            verify(settlementRepository, never()).save(any(Settlement.class));
            verify(debtRepository, never()).save(any(Debt.class));
            verify(debtRepository, never()).delete(any(Debt.class));
        }

        @Test
        void settleDebt_withZeroAmount_throwsIllegalArgumentExceptionAndDoesNotWrite() {
            SettlementCreateRequestDTO requestDTO = new SettlementCreateRequestDTO(
                    debtId,
                    creditorId,
                    BigDecimal.ZERO,
                    null
            );

            when(debtRepository.findById(debtId)).thenReturn(Optional.of(debt));
            when(userRepository.findById(debtorId)).thenReturn(Optional.of(debtor));

            assertThatThrownBy(() -> settlementService.settleDebt(debtorId, requestDTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Settlement amount must be strictly greater than zero");

            verify(settlementRepository, never()).save(any(Settlement.class));
            verify(debtRepository, never()).save(any(Debt.class));
            verify(debtRepository, never()).delete(any(Debt.class));
        }
    }

    @Nested
    class GetFilteredSettlementsTests {

        @Test
        void getFilteredSettlements_withNullUserId_throwsIllegalArgumentException() {
            TransactionFilterRequestDTO filter = new TransactionFilterRequestDTO(householdId, UserTransactionRole.ALL, null, null);

            assertThatThrownBy(() -> settlementService.getFilteredSettlements(null, filter))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User ID must not be null");

            verifyNoInteractions(userRepository, settlementRepository);
        }

        @Test
        void getFilteredSettlements_withNullFilter_throwsIllegalArgumentException() {
            assertThatThrownBy(() -> settlementService.getFilteredSettlements(debtorId, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Filter DTO must not be null");

            verifyNoInteractions(userRepository, settlementRepository);
        }

        @Test
        void getFilteredSettlements_userNotFound_throwsIllegalArgumentException() {
            TransactionFilterRequestDTO filter = new TransactionFilterRequestDTO(householdId, UserTransactionRole.ALL, null, null);
            when(userRepository.findById(debtorId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> settlementService.getFilteredSettlements(debtorId, filter))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User not found with ID");

            verify(settlementRepository, never()).findAll(any(Specification.class));
        }

        @Test
        void getFilteredSettlements_usesFilterHouseholdWhenProvided() {
            TransactionFilterRequestDTO filter = new TransactionFilterRequestDTO(householdId, UserTransactionRole.ALL, null, null);
            when(userRepository.findById(debtorId)).thenReturn(Optional.of(debtor));

            @SuppressWarnings("unchecked")
            Specification<Settlement> spec = mock(Specification.class);

            try (MockedStatic<QuerySpecification> querySpec = mockStatic(QuerySpecification.class)) {
                querySpec.when(() -> QuerySpecification.buildSettlementFilter(eq(debtorId), eq(householdId), same(filter)))
                        .thenReturn(spec);
                when(settlementRepository.findAll(spec)).thenReturn(Collections.emptyList());

                List<SettlementResponseDTO> result = settlementService.getFilteredSettlements(debtorId, filter);

                assertThat(result).isEmpty();
                querySpec.verify(() -> QuerySpecification.buildSettlementFilter(eq(debtorId), eq(householdId), same(filter)));
                verify(settlementRepository).findAll(spec);
            }
        }

        @Test
        void getFilteredSettlements_usesUserHouseholdWhenFilterHouseholdMissing() {
            TransactionFilterRequestDTO filter = new TransactionFilterRequestDTO(null, UserTransactionRole.ALL, null, null);
            when(userRepository.findById(debtorId)).thenReturn(Optional.of(debtor));

            @SuppressWarnings("unchecked")
            Specification<Settlement> spec = mock(Specification.class);

            try (MockedStatic<QuerySpecification> querySpec = mockStatic(QuerySpecification.class)) {
                querySpec.when(() -> QuerySpecification.buildSettlementFilter(eq(debtorId), eq(householdId), same(filter)))
                        .thenReturn(spec);
                when(settlementRepository.findAll(spec)).thenReturn(Collections.emptyList());

                List<SettlementResponseDTO> result = settlementService.getFilteredSettlements(debtorId, filter);

                assertThat(result).isEmpty();
                querySpec.verify(() -> QuerySpecification.buildSettlementFilter(eq(debtorId), eq(householdId), same(filter)));
            }
        }

        @Test
        void getFilteredSettlements_whenUserHasNoHouseholdAndNoFilterHousehold_usesNullHousehold() {
            TransactionFilterRequestDTO filter = new TransactionFilterRequestDTO(null, UserTransactionRole.ALL, null, null);
            User userWithoutMembership = createUser(debtorId, "No", "House");
            userWithoutMembership.setHouseholdMembership(null);
            when(userRepository.findById(debtorId)).thenReturn(Optional.of(userWithoutMembership));

            @SuppressWarnings("unchecked")
            Specification<Settlement> spec = mock(Specification.class);

            try (MockedStatic<QuerySpecification> querySpec = mockStatic(QuerySpecification.class)) {
                querySpec.when(() -> QuerySpecification.buildSettlementFilter(eq(debtorId), eq(null), same(filter)))
                        .thenReturn(spec);
                when(settlementRepository.findAll(spec)).thenReturn(Collections.emptyList());

                List<SettlementResponseDTO> result = settlementService.getFilteredSettlements(debtorId, filter);

                assertThat(result).isEmpty();
                querySpec.verify(() -> QuerySpecification.buildSettlementFilter(eq(debtorId), eq(null), same(filter)));
                verify(settlementRepository).findAll(spec);
            }
        }

        @Test
        void getFilteredSettlements_mapsResponseForDebtorRole() {
            TransactionFilterRequestDTO filter = new TransactionFilterRequestDTO(householdId, UserTransactionRole.DEBTOR, null, null);
            when(userRepository.findById(debtorId)).thenReturn(Optional.of(debtor));

            Settlement settlement = new Settlement(debt, debtor, creditor, new BigDecimal("25.00"), null);
            settlement.setId(UUID.randomUUID());

            @SuppressWarnings("unchecked")
            Specification<Settlement> spec = mock(Specification.class);

            try (MockedStatic<QuerySpecification> querySpec = mockStatic(QuerySpecification.class)) {
                querySpec.when(() -> QuerySpecification.buildSettlementFilter(eq(debtorId), eq(householdId), same(filter)))
                        .thenReturn(spec);
                when(settlementRepository.findAll(spec)).thenReturn(List.of(settlement));

                List<SettlementResponseDTO> result = settlementService.getFilteredSettlements(debtorId, filter);

                assertThat(result).hasSize(1);
                SettlementResponseDTO dto = result.get(0);
                assertThat(dto.userTransactionRole()).isEqualTo(UserTransactionRole.DEBTOR);
                assertThat(dto.involvedId()).isEqualTo(creditorId);
                assertThat(dto.involvedName()).isEqualTo("Bob Creditor");
                assertThat(dto.amount()).isEqualByComparingTo("25.00");
                assertThat(dto.description()).isNull(); // Assuming it defaults to null or matching what was set in Settlement constructor
            }
        }

        @Test
        void getFilteredSettlements_mapsResponseForCreditorRole() {
            TransactionFilterRequestDTO filter = new TransactionFilterRequestDTO(householdId, UserTransactionRole.CREDITOR, null, null);
            when(userRepository.findById(creditorId)).thenReturn(Optional.of(creditor));
            creditor.setHouseholdMembership(debtor.getHouseholdMembership());

            Settlement settlement = new Settlement(debt, debtor, creditor, new BigDecimal("25.00"), null);
            settlement.setId(UUID.randomUUID());

            @SuppressWarnings("unchecked")
            Specification<Settlement> spec = mock(Specification.class);

            try (MockedStatic<QuerySpecification> querySpec = mockStatic(QuerySpecification.class)) {
                querySpec.when(() -> QuerySpecification.buildSettlementFilter(eq(creditorId), eq(householdId), same(filter)))
                        .thenReturn(spec);
                when(settlementRepository.findAll(spec)).thenReturn(List.of(settlement));

                List<SettlementResponseDTO> result = settlementService.getFilteredSettlements(creditorId, filter);

                assertThat(result).hasSize(1);
                SettlementResponseDTO dto = result.get(0);
                assertThat(dto.userTransactionRole()).isEqualTo(UserTransactionRole.CREDITOR);
                assertThat(dto.involvedId()).isEqualTo(debtorId);
                assertThat(dto.involvedName()).isEqualTo("Alice Debtor");
            }
        }

        @Test
        void getFilteredSettlements_mapsResponseForAllRole_resolvesToOtherParty() {
            // Arrange
            TransactionFilterRequestDTO filter = new TransactionFilterRequestDTO(householdId, UserTransactionRole.ALL, null, null);
            
            when(userRepository.findById(debtorId)).thenReturn(Optional.of(debtor));

            Settlement settlement = new Settlement(debt, debtor, creditor, new BigDecimal("25.00"), null);
            settlement.setId(UUID.randomUUID());

            @SuppressWarnings("unchecked")
            Specification<Settlement> spec = mock(Specification.class);

            try (MockedStatic<QuerySpecification> querySpec = mockStatic(QuerySpecification.class)) {
                querySpec.when(() -> QuerySpecification.buildSettlementFilter(eq(debtorId), eq(householdId), same(filter)))
                        .thenReturn(spec);
                when(settlementRepository.findAll(spec)).thenReturn(List.of(settlement));

                // Act
                List<SettlementResponseDTO> result = settlementService.getFilteredSettlements(debtorId, filter);

                // Assert
                assertThat(result).hasSize(1);
                SettlementResponseDTO dto = result.get(0);
                assertThat(dto.userTransactionRole()).isEqualTo(UserTransactionRole.ALL);
                
                assertThat(dto.involvedId()).isEqualTo(creditorId);
                assertThat(dto.involvedName()).isEqualTo("Bob Creditor");
            }
        }

        @Test
        void getFilteredSettlements_whenRoleIsNull_throwsIllegalArgumentException() {
            TransactionFilterRequestDTO filter = new TransactionFilterRequestDTO(householdId, null, null, null);
            when(userRepository.findById(debtorId)).thenReturn(Optional.of(debtor));

            Settlement settlement = new Settlement(debt, debtor, creditor, new BigDecimal("25.00"), null);
            settlement.setId(UUID.randomUUID());

            @SuppressWarnings("unchecked")
            Specification<Settlement> spec = mock(Specification.class);

            try (MockedStatic<QuerySpecification> querySpec = mockStatic(QuerySpecification.class)) {
                querySpec.when(() -> QuerySpecification.buildSettlementFilter(eq(debtorId), eq(householdId), same(filter)))
                        .thenReturn(spec);
                when(settlementRepository.findAll(spec)).thenReturn(List.of(settlement));

                assertThatThrownBy(() -> settlementService.getFilteredSettlements(debtorId, filter))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("Invalid user role for settlement response");
            }
        }
    }

    private User createUser(UUID id, String name, String surname) {
        User user = new User();
        user.setName(name);
        user.setSurname(surname);
        user.setEmail(name.toLowerCase() + "@test.com");
        user.setPassword("password");
        user.setId(id);
        return user;
    }
}