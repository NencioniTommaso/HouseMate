package com.housemate.backend.repository.user;

import com.housemate.backend.model.household.Household;
import com.housemate.backend.model.household.HouseholdMembership;
import com.housemate.backend.model.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("User Repository Integration Tests")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User createPersistedUser(String name, String surname, String email, String iban, String paymentLink) {
        User user = new User(name, surname, email, "encoded-password");
        user.setIban(iban);
        user.setPaymentLink(paymentLink);
        return entityManager.persist(user);
    }

    private HouseholdMembership createPersistedMembership(User user, String householdName, boolean isAdmin) {
        Household household = new Household();
        household.setName(householdName);
        household.setMemberships(new ArrayList<>());
        household = entityManager.persist(household);

        HouseholdMembership membership = new HouseholdMembership(household, user, isAdmin);
        household.getMemberships().add(membership);
        user.setHouseholdMembership(membership);
        entityManager.persist(membership);

        return membership;
    }

    @Test
    @DisplayName("findByEmail - should return user when email exists")
    void findByEmail_shouldReturnUser_whenEmailExists() {
        User persistedUser = createPersistedUser(
            "Mario",
            "Rossi",
            "mario.rossi@example.com",
            "IT60X0542811101000000123456",
            "https://pay.example.com/mario"
        );

        entityManager.flush();
        entityManager.clear();

        Optional<User> result = userRepository.findByEmail("mario.rossi@example.com");

        assertThat(result)
            .isPresent()
            .get()
            .extracting(User::getId)
            .isEqualTo(persistedUser.getId());
    }

    @Test
    @DisplayName("findByEmail - should return empty when email does not exist")
    void findByEmail_shouldReturnEmpty_whenEmailDoesNotExist() {
        createPersistedUser(
            "Luigi",
            "Verdi",
            "luigi.verdi@example.com",
            "IT02A0306909606100000123456",
            null
        );

        entityManager.flush();
        entityManager.clear();

        Optional<User> result = userRepository.findByEmail("missing.user@example.com");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("existsByEmail - should return true only for persisted email")
    void existsByEmail_shouldReturnExpectedResult() {
        createPersistedUser(
            "Alice",
            "Bianchi",
            "alice.bianchi@example.com",
            "IT58C0501803200000012345678",
            null
        );

        entityManager.flush();

        assertThat(userRepository.existsByEmail("alice.bianchi@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("not.present@example.com")).isFalse();
    }

    @Test
    @DisplayName("existsByEmailAndIdNot - should return true when another user already has the email")
    void existsByEmailAndIdNot_shouldReturnTrue_whenEmailBelongsToAnotherUser() {
        User owner = createPersistedUser(
            "Paolo",
            "Neri",
            "paolo.neri@example.com",
            "IT14D0335901600100000123456",
            null
        );
        User anotherUser = createPersistedUser(
            "Marco",
            "Blu",
            "marco.blu@example.com",
            "IT12E0542811101000000654321",
            null
        );

        entityManager.flush();

        assertThat(userRepository.existsByEmailAndIdNot(owner.getEmail(), anotherUser.getId())).isTrue();
    }

    @Test
    @DisplayName("existsByEmailAndIdNot - should return false when email belongs to same user")
    void existsByEmailAndIdNot_shouldReturnFalse_whenEmailBelongsToSameUser() {
        User owner = createPersistedUser(
            "Giulia",
            "Rosa",
            "giulia.rosa@example.com",
            "IT33F0200802212000001234567",
            null
        );

        entityManager.flush();

        assertThat(userRepository.existsByEmailAndIdNot(owner.getEmail(), owner.getId())).isFalse();
    }

    @Test
    @DisplayName("findByHouseholdMembership - should return users belonging to the membership")
    void findByHouseholdMembership_shouldReturnUsers_whenMembershipExists() {
        User user = createPersistedUser(
            "Sara",
            "Gialli",
            "sara.gialli@example.com",
            "IT45G0303203280000001234567",
            null
        );
        HouseholdMembership membership = createPersistedMembership(user, "HouseMate Home", true);

        entityManager.flush();
        entityManager.clear();

        List<User> users = userRepository.findByHouseholdMembership(membership);

        assertThat(users)
            .hasSize(1)
            .extracting(User::getEmail)
            .containsExactly("sara.gialli@example.com");
    }

    @Test
    @DisplayName("existsByHouseholdMembership - should return true only for persisted memberships")
    void existsByHouseholdMembership_shouldReturnExpectedResult() {
        User user = createPersistedUser(
            "Luca",
            "Arancio",
            "luca.arancio@example.com",
            "IT77H0100511710100000001234",
            null
        );
        HouseholdMembership persistedMembership = createPersistedMembership(user, "Orange Home", false);

        entityManager.flush();

        assertThat(userRepository.existsByHouseholdMembership(persistedMembership)).isTrue();
        assertThat(userRepository.existsByHouseholdMembership(new HouseholdMembership())).isFalse();
    }
}