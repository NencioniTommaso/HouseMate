package com.housemate.backend.service;

import com.housemate.backend.model.household.Household;
import com.housemate.backend.model.household.HouseholdMembership;
import com.housemate.backend.model.user.User;
import com.housemate.backend.repository.household.HouseholdMembershipRepository;
import com.housemate.backend.repository.household.HouseholdRepository;
import com.housemate.backend.repository.user.UserRepository;
import com.housemate.shared.dto.household.request.AddMemberRequestDTO;
import com.housemate.shared.dto.household.request.HouseholdCreateRequestDTO;
import com.housemate.shared.dto.household.response.HouseholdInvitationCodeResponseDTO;
import com.housemate.shared.dto.household.response.HouseholdMemberResponseDTO;
import com.housemate.shared.dto.household.response.HouseholdResponseDTO;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("HouseholdService Integration Tests")
@SuppressWarnings("null")
class HouseholdServiceIntegrationTest {

    @Autowired
    private HouseholdService householdService;

    @Autowired
    private HouseholdRepository householdRepository;

    @Autowired
    private HouseholdMembershipRepository householdMembershipRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("shouldCreateHouseholdAndAssignAdminMembership creates household and assigns admin role to creator")
    void shouldCreateHouseholdAndAssignAdminMembership() {
        User creator = persistUser("Alice", "Creator", "alice.creator@test.com");

        HouseholdCreateRequestDTO request = new HouseholdCreateRequestDTO("Emerald Villa");
        HouseholdResponseDTO response = householdService.createHousehold(creator.getId(), request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo("Emerald Villa");
        assertThat(response.memberships()).hasSize(1);

        HouseholdMemberResponseDTO memberResponse = response.memberships().get(0);
        assertThat(memberResponse.user().id()).isEqualTo(creator.getId());
        assertThat(memberResponse.user().email()).isEqualTo("alice.creator@test.com");
        assertThat(memberResponse.membership().isAdmin()).isTrue();

        // Flush and clear cache to verify database state
        entityManager.flush();
        entityManager.clear();

        Household persistedHousehold = householdRepository.findById(response.id()).orElseThrow();
        assertThat(persistedHousehold.getName()).isEqualTo("Emerald Villa");
        assertThat(persistedHousehold.getDate()).isNotNull();
        assertThat(persistedHousehold.getInvitationCode()).isNotBlank();
        assertThat(persistedHousehold.getInvitationCodeRefreshedAt()).isNotNull();

        User reloadedCreator = userRepository.findById(creator.getId()).orElseThrow();
        assertThat(reloadedCreator.getHouseholdMembership()).isNotNull();
        assertThat(reloadedCreator.getHouseholdMembership().getHousehold().getId()).isEqualTo(response.id());
        assertThat(reloadedCreator.getHouseholdMembership().isAdmin()).isTrue();
    }

    @Test
    @DisplayName("shouldGenerateInvitationCodeAndAllowNewMemberToJoin retrieves invitation code and allows new member to join as non-admin")
    void shouldGenerateInvitationCodeAndAllowNewMemberToJoin() {
        User admin = persistUser("Alice", "Admin", "alice.admin@test.com");
        HouseholdResponseDTO createdHousehold = householdService.createHousehold(
            admin.getId(),
            new HouseholdCreateRequestDTO("Cozy Cottage")
        );

        HouseholdInvitationCodeResponseDTO codeResponse = householdService.getInvitationCode(admin.getId());
        assertThat(codeResponse.invitationCode()).isNotBlank();
        assertThat(codeResponse.refreshedAt()).isNotNull();

        User joiningUser = persistUser("Bob", "Joiner", "bob.joiner@test.com");

        HouseholdResponseDTO joinedHousehold = householdService.addMember(
            joiningUser.getId(),
            new AddMemberRequestDTO(codeResponse.invitationCode())
        );

        assertThat(joinedHousehold).isNotNull();
        assertThat(joinedHousehold.id()).isEqualTo(createdHousehold.id());
        assertThat(joinedHousehold.memberships()).hasSize(2);

        HouseholdMemberResponseDTO bobMembershipDto = joinedHousehold.memberships().stream()
            .filter(member -> member.user().id().equals(joiningUser.getId()))
            .findFirst()
            .orElseThrow();
        assertThat(bobMembershipDto.membership().isAdmin()).isFalse();

        // Flush and clear to verify persisted state
        entityManager.flush();
        entityManager.clear();

        Household reloadedHousehold = householdRepository.findById(createdHousehold.id()).orElseThrow();
        List<HouseholdMembership> memberships = householdMembershipRepository.findByHousehold(reloadedHousehold);
        assertThat(memberships).hasSize(2);

        HouseholdResponseDTO bobRetrievedHousehold = householdService.getCurrentUserHousehold(joiningUser.getId());
        assertThat(bobRetrievedHousehold.id()).isEqualTo(createdHousehold.id());

        User reloadedBob = userRepository.findById(joiningUser.getId()).orElseThrow();
        assertThat(reloadedBob.getHouseholdMembership()).isNotNull();
        assertThat(reloadedBob.getHouseholdMembership().getHousehold().getId()).isEqualTo(createdHousehold.id());
        assertThat(reloadedBob.getHouseholdMembership().isAdmin()).isFalse();
    }

    @Test
    @DisplayName("shouldFailWhenJoiningWithInvalidCode rejects membership request and keeps user unassigned")
    void shouldFailWhenJoiningWithInvalidCode() {
        User outsider = persistUser("Charlie", "Outsider", "charlie.outsider@test.com");

        AddMemberRequestDTO invalidRequest = new AddMemberRequestDTO("INVALID_INVITATION_CODE_XYZ");

        assertThatThrownBy(() -> householdService.addMember(outsider.getId(), invalidRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid household invitation code.");

        entityManager.flush();
        entityManager.clear();

        User reloadedOutsider = userRepository.findById(outsider.getId()).orElseThrow();
        assertThat(reloadedOutsider.getHouseholdMembership()).isNull();
    }

    @Test
    @DisplayName("shouldAllowMemberToLeaveOrBeRemoved allows admin removal, member departure, and deletes household when empty")
    void shouldAllowMemberToLeaveOrBeRemoved() {
        User admin = persistUser("Alice", "Admin", "alice.lifecycle@test.com");
        HouseholdResponseDTO householdDto = householdService.createHousehold(
            admin.getId(),
            new HouseholdCreateRequestDTO("Lifecycle Manor")
        );
        UUID householdId = householdDto.id();

        HouseholdInvitationCodeResponseDTO codeResponse = householdService.getInvitationCode(admin.getId());

        User memberBob = persistUser("Bob", "Member", "bob.lifecycle@test.com");
        User memberCharlie = persistUser("Charlie", "Member", "charlie.lifecycle@test.com");

        householdService.addMember(memberBob.getId(), new AddMemberRequestDTO(codeResponse.invitationCode()));
        householdService.addMember(memberCharlie.getId(), new AddMemberRequestDTO(codeResponse.invitationCode()));

        entityManager.flush();
        entityManager.clear();

        // 1. Admin removes Bob
        HouseholdResponseDTO afterRemoval = householdService.removeMember(admin.getId(), memberBob.getId());
        assertThat(afterRemoval.memberships()).hasSize(2);
        assertThat(afterRemoval.memberships().stream().noneMatch(m -> m.user().id().equals(memberBob.getId()))).isTrue();

        entityManager.flush();
        entityManager.clear();

        User reloadedBob = userRepository.findById(memberBob.getId()).orElseThrow();
        assertThat(reloadedBob.getHouseholdMembership()).isNull();
        assertThatThrownBy(() -> householdService.getCurrentUserHousehold(memberBob.getId()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("does not belong to any household");

        // 2. Charlie leaves voluntarily
        householdService.leaveHousehold(memberCharlie.getId());

        entityManager.flush();
        entityManager.clear();

        User reloadedCharlie = userRepository.findById(memberCharlie.getId()).orElseThrow();
        assertThat(reloadedCharlie.getHouseholdMembership()).isNull();

        Household remainingHousehold = householdRepository.findById(householdId).orElseThrow();
        List<HouseholdMembership> remainingMemberships = householdMembershipRepository.findByHousehold(remainingHousehold);
        assertThat(remainingMemberships).hasSize(1);
        assertThat(remainingMemberships.get(0).getUser().getId()).isEqualTo(admin.getId());

        // 3. Admin (sole remaining member) leaves -> Household is deleted
        householdService.leaveHousehold(admin.getId());

        entityManager.flush();
        entityManager.clear();

        User reloadedAdmin = userRepository.findById(admin.getId()).orElseThrow();
        assertThat(reloadedAdmin.getHouseholdMembership()).isNull();
        assertThat(householdRepository.findById(householdId)).isEmpty();
    }

    private User persistUser(String name, String surname, String email) {
        User user = new User(name, surname, email, "encodedPassword123!");
        return userRepository.saveAndFlush(user);
    }
}
