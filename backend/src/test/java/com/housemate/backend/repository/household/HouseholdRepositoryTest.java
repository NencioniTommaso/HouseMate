package com.housemate.backend.repository.household;

import com.housemate.backend.model.household.Household;
import com.housemate.backend.model.household.HouseholdMembership;
import com.housemate.backend.model.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("Household Repository Integration Tests")
class HouseholdRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private HouseholdRepository householdRepository;

    private Household createPersistedHouseholdWithMember(String householdName, String email) {
        return createPersistedHouseholdWithMember(
            householdName,
            email,
            "inv-" + UUID.randomUUID().toString().replace("-", "")
        );
    }

    private Household createPersistedHouseholdWithMember(String householdName, String email, String invitationCode) {
        Household household = new Household();
        household.setName(householdName);
        household.setMemberships(new ArrayList<>());
        household.setInvitationCode(invitationCode);
        household.setInvitationCodeRefreshedAt(LocalDateTime.of(2026, 4, 3, 10, 0));
        household = entityManager.persist(household);

        User user = new User("Test", "User", email, "password");
        user = entityManager.persist(user);

        HouseholdMembership membership = new HouseholdMembership(household, user, true);
        household.getMemberships().add(membership);
        user.setHouseholdMembership(membership);
        entityManager.persist(membership);

        return household;
    }

    @Test
    @DisplayName("findByName - should return household when entity exists")
    void findByName_shouldReturnHousehold_whenEntityExists() {
        Household household = createPersistedHouseholdWithMember("Alpha Home", "alpha.member@test.com");

        entityManager.flush();
        entityManager.clear();

        Optional<Household> result = householdRepository.findByName("Alpha Home");

        assertThat(result)
            .isPresent()
            .get()
            .extracting(Household::getId)
            .isEqualTo(household.getId());
    }

    @Test
    @DisplayName("findByMemberships_User_Id - should return household when user is a member")
    void findByMembershipsUserId_shouldReturnHousehold_whenMembershipExists() {
        Household household = new Household();
        household.setName("Beta Home");
        household.setMemberships(new ArrayList<>());
        household.setInvitationCode("inv-code-beta-123");
        household.setInvitationCodeRefreshedAt(LocalDateTime.of(2026, 4, 3, 10, 30));
        household = entityManager.persist(household);

        User user = new User("Mario", "Rossi", "mario.rossi@test.com", "password");
        user = entityManager.persist(user);

        HouseholdMembership membership = new HouseholdMembership(household, user, true);
        household.getMemberships().add(membership);
        user.setHouseholdMembership(membership);
        entityManager.persist(membership);

        entityManager.flush();
        entityManager.clear();

        UUID userId = user.getId();
        Optional<Household> result = householdRepository.findByMemberships_User_Id(userId);

        assertThat(result)
            .isPresent()
            .get()
            .extracting(Household::getId)
            .isEqualTo(household.getId());
    }

    @Test
    @DisplayName("findByInvitationCode - should return household when invitation code exists")
    void findByInvitationCode_shouldReturnHousehold_whenCodeExists() {
        String invitationCode = "inv-code-lookup-456";
        Household household = createPersistedHouseholdWithMember("Lookup Home", "lookup.member@test.com", invitationCode);

        entityManager.flush();
        entityManager.clear();

        Optional<Household> result = householdRepository.findByInvitationCode(invitationCode);

        assertThat(result)
            .isPresent()
            .get()
            .extracting(Household::getId)
            .isEqualTo(household.getId());
    }

    @Test
    @DisplayName("findByMemberships_User_Id - should return empty when user is not a member of any household")
    void findByMembershipsUserId_shouldReturnEmpty_whenMembershipDoesNotExist() {
        User user = new User("Luigi", "Verdi", "luigi.verdi@test.com", "password");
        user = entityManager.persist(user);

        entityManager.flush();
        entityManager.clear();

        Optional<Household> result = householdRepository.findByMemberships_User_Id(user.getId());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("existsByName - should return true only for persisted household name")
    void existsByName_shouldReturnExpectedResult() {
        createPersistedHouseholdWithMember("Gamma Home", "gamma.member@test.com");

        entityManager.flush();

        assertThat(householdRepository.existsByName("Gamma Home")).isTrue();
        assertThat(householdRepository.existsByName("Missing Home")).isFalse();
    }

    @Test
    @DisplayName("existsByInvitationCode - should return true only for persisted invitation code")
    void existsByInvitationCode_shouldReturnExpectedResult() {
        String invitationCode = "inv-code-gamma-123";
        createPersistedHouseholdWithMember("Delta Home", "delta.member@test.com", invitationCode);

        entityManager.flush();

        assertThat(householdRepository.existsByInvitationCode(invitationCode)).isTrue();
        assertThat(householdRepository.existsByInvitationCode("inv-missing-code")).isFalse();
    }
}
