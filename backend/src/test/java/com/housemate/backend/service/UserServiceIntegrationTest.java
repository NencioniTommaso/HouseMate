package com.housemate.backend.service;

import com.housemate.backend.model.user.User;
import com.housemate.backend.repository.user.UserRepository;
import com.housemate.shared.dto.user.request.UserUpdateRequestDTO;
import com.housemate.shared.dto.user.response.UserResponseDTO;
import com.housemate.shared.utils.types.DateRange;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("UserService Integration Tests")
@SuppressWarnings("null")
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private static final String VALID_IBAN_1 = "IT60X0542811101000000123456";
    private static final String VALID_IBAN_2 = "DE89370400440532013000";
    private static final String VALID_PAYMENT_LINK_1 = "https://paypal.me/alice";
    private static final String VALID_PAYMENT_LINK_2 = "https://revolut.me/aliceupdated";

    // ==========================================
    // getCurrentUser Scenarios
    // ==========================================

    @Test
    @DisplayName("getCurrentUser - should return user profile details when user exists")
    void getCurrentUser_withExistingUser_shouldReturnUserResponseDTO() {
        User user = persistUser("Alice", "Smith", "alice.smith@example.com", VALID_IBAN_1, VALID_PAYMENT_LINK_1);

        UserResponseDTO response = userService.getCurrentUser(user.getId());

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(user.getId());
        assertThat(response.name()).isEqualTo("Alice");
        assertThat(response.surname()).isEqualTo("Smith");
        assertThat(response.email()).isEqualTo("alice.smith@example.com");
        assertThat(response.iban()).isEqualTo(VALID_IBAN_1);
        assertThat(response.paymentLink()).isEqualTo(VALID_PAYMENT_LINK_1);
    }

    @Test
    @DisplayName("getCurrentUser - should throw IllegalArgumentException when user does not exist")
    void getCurrentUser_withNonExistentUser_shouldThrowIllegalArgumentException() {
        UUID nonExistentUserId = UUID.randomUUID();

        assertThatThrownBy(() -> userService.getCurrentUser(nonExistentUserId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("not found");
    }

    // ==========================================
    // updateCurrentUser Scenarios
    // ==========================================

    @Test
    @DisplayName("updateCurrentUser - should update all profile details and persist changes to database")
    void updateCurrentUser_allFields_shouldUpdateAndPersistChanges() {
        User user = persistUser("Bob", "OldName", "bob.old@example.com", VALID_IBAN_1, VALID_PAYMENT_LINK_1);

        UserUpdateRequestDTO updateRequest = new UserUpdateRequestDTO(
            "Robert",
            "NewName",
            "robert.new@example.com",
            VALID_IBAN_2,
            VALID_PAYMENT_LINK_2
        );

        UserResponseDTO response = userService.updateCurrentUser(user.getId(), updateRequest);

        // Verify response DTO
        assertThat(response.id()).isEqualTo(user.getId());
        assertThat(response.name()).isEqualTo("Robert");
        assertThat(response.surname()).isEqualTo("NewName");
        assertThat(response.email()).isEqualTo("robert.new@example.com");
        assertThat(response.iban()).isEqualTo(VALID_IBAN_2);
        assertThat(response.paymentLink()).isEqualTo(VALID_PAYMENT_LINK_2);

        // Flush and clear Hibernate cache to verify actual database state
        entityManager.flush();
        entityManager.clear();

        User persistedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(persistedUser.getName()).isEqualTo("Robert");
        assertThat(persistedUser.getSurname()).isEqualTo("NewName");
        assertThat(persistedUser.getEmail()).isEqualTo("robert.new@example.com");
        assertThat(persistedUser.getIban()).isEqualTo(VALID_IBAN_2);
        assertThat(persistedUser.getPaymentLink()).isEqualTo(VALID_PAYMENT_LINK_2);
    }

    @Test
    @DisplayName("updateCurrentUser - should only update provided non-null fields and leave others unchanged")
    void updateCurrentUser_partialFields_shouldOnlyUpdateSpecifiedFields() {
        User user = persistUser("Charlie", "Brown", "charlie.brown@example.com", VALID_IBAN_1, VALID_PAYMENT_LINK_1);

        UserUpdateRequestDTO partialRequest = new UserUpdateRequestDTO(
            "Charles",
            null,
            null,
            null,
            VALID_PAYMENT_LINK_2
        );

        UserResponseDTO response = userService.updateCurrentUser(user.getId(), partialRequest);

        assertThat(response.name()).isEqualTo("Charles");
        assertThat(response.surname()).isEqualTo("Brown");
        assertThat(response.email()).isEqualTo("charlie.brown@example.com");
        assertThat(response.iban()).isEqualTo(VALID_IBAN_1);
        assertThat(response.paymentLink()).isEqualTo(VALID_PAYMENT_LINK_2);

        entityManager.flush();
        entityManager.clear();

        User reloadedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(reloadedUser.getName()).isEqualTo("Charles");
        assertThat(reloadedUser.getSurname()).isEqualTo("Brown");
        assertThat(reloadedUser.getEmail()).isEqualTo("charlie.brown@example.com");
        assertThat(reloadedUser.getIban()).isEqualTo(VALID_IBAN_1);
        assertThat(reloadedUser.getPaymentLink()).isEqualTo(VALID_PAYMENT_LINK_2);
    }

    @Test
    @DisplayName("updateCurrentUser - should return current details when update DTO contains no changes")
    void updateCurrentUser_emptyDto_shouldReturnUnmodifiedUser() {
        User user = persistUser("Diana", "Prince", "diana.prince@example.com", VALID_IBAN_1, VALID_PAYMENT_LINK_1);

        UserUpdateRequestDTO emptyRequest = new UserUpdateRequestDTO(null, null, null, null, null);

        UserResponseDTO response = userService.updateCurrentUser(user.getId(), emptyRequest);

        assertThat(response.name()).isEqualTo("Diana");
        assertThat(response.surname()).isEqualTo("Prince");
        assertThat(response.email()).isEqualTo("diana.prince@example.com");
        assertThat(response.iban()).isEqualTo(VALID_IBAN_1);
        assertThat(response.paymentLink()).isEqualTo(VALID_PAYMENT_LINK_1);
    }

    @Test
    @DisplayName("updateCurrentUser - should succeed when updating email to the current user's existing email")
    void updateCurrentUser_sameEmail_shouldSucceedWithoutConflict() {
        User user = persistUser("Evan", "Wright", "evan.wright@example.com", VALID_IBAN_1, null);

        UserUpdateRequestDTO request = new UserUpdateRequestDTO("Evan Updated", null, "evan.wright@example.com", null, null);

        UserResponseDTO response = userService.updateCurrentUser(user.getId(), request);

        assertThat(response.name()).isEqualTo("Evan Updated");
        assertThat(response.email()).isEqualTo("evan.wright@example.com");
    }

    @Test
    @DisplayName("updateCurrentUser - should throw IllegalArgumentException when email is already taken by another user")
    void updateCurrentUser_duplicateEmail_shouldThrowIllegalArgumentException() {
        User user1 = persistUser("UserOne", "Test", "user1@example.com", VALID_IBAN_1, null);
        User user2 = persistUser("UserTwo", "Test", "user2@example.com", VALID_IBAN_2, null);

        UserUpdateRequestDTO conflictRequest = new UserUpdateRequestDTO(null, null, "user1@example.com", null, null);

        assertThatThrownBy(() -> userService.updateCurrentUser(user2.getId(), conflictRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Email already registered");

        entityManager.flush();
        entityManager.clear();

        User reloadedUser2 = userRepository.findById(user2.getId()).orElseThrow();
        assertThat(reloadedUser2.getEmail()).isEqualTo("user2@example.com");
    }

    @Test
    @DisplayName("updateCurrentUser - should throw IllegalArgumentException when blank name is provided")
    void updateCurrentUser_blankName_shouldThrowIllegalArgumentException() {
        User user = persistUser("Fiona", "Gallagher", "fiona@example.com", VALID_IBAN_1, null);

        UserUpdateRequestDTO blankNameRequest = new UserUpdateRequestDTO("   ", null, null, null, null);

        assertThatThrownBy(() -> userService.updateCurrentUser(user.getId(), blankNameRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Name cannot be blank");
    }

    @Test
    @DisplayName("updateCurrentUser - should throw IllegalArgumentException when blank surname is provided")
    void updateCurrentUser_blankSurname_shouldThrowIllegalArgumentException() {
        User user = persistUser("George", "Clark", "george@example.com", VALID_IBAN_1, null);

        UserUpdateRequestDTO blankSurnameRequest = new UserUpdateRequestDTO(null, "   ", null, null, null);

        assertThatThrownBy(() -> userService.updateCurrentUser(user.getId(), blankSurnameRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Surname cannot be blank");
    }

    @Test
    @DisplayName("updateCurrentUser - should throw IllegalArgumentException when invalid email format is provided")
    void updateCurrentUser_invalidEmail_shouldThrowIllegalArgumentException() {
        User user = persistUser("Hannah", "Abbott", "hannah@example.com", VALID_IBAN_1, null);

        UserUpdateRequestDTO invalidEmailRequest = new UserUpdateRequestDTO(null, null, "invalid-email-format", null, null);

        assertThatThrownBy(() -> userService.updateCurrentUser(user.getId(), invalidEmailRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Email must be a valid email address");
    }

    @Test
    @DisplayName("updateCurrentUser - should throw IllegalArgumentException when invalid IBAN format is provided")
    void updateCurrentUser_invalidIban_shouldThrowIllegalArgumentException() {
        User user = persistUser("Ian", "Malcolm", "ian@example.com", VALID_IBAN_1, null);

        UserUpdateRequestDTO invalidIbanRequest = new UserUpdateRequestDTO(null, null, null, "INVALID_IBAN_XYZ", null);

        assertThatThrownBy(() -> userService.updateCurrentUser(user.getId(), invalidIbanRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("IBAN must be a valid IBAN format");
    }

    @Test
    @DisplayName("updateCurrentUser - should throw IllegalArgumentException when invalid payment link format is provided")
    void updateCurrentUser_invalidPaymentLink_shouldThrowIllegalArgumentException() {
        User user = persistUser("Jack", "Sparrow", "jack@example.com", VALID_IBAN_1, null);

        UserUpdateRequestDTO invalidPaymentLinkRequest = new UserUpdateRequestDTO(null, null, null, null, "ftp://invalid-scheme");

        assertThatThrownBy(() -> userService.updateCurrentUser(user.getId(), invalidPaymentLinkRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Payment link must be a valid URL starting with http:// or https://");
    }

    @Test
    @DisplayName("updateCurrentUser - should throw IllegalArgumentException when user does not exist")
    void updateCurrentUser_nonExistentUser_shouldThrowIllegalArgumentException() {
        UUID nonExistentId = UUID.randomUUID();
        UserUpdateRequestDTO request = new UserUpdateRequestDTO("Ghost", null, null, null, null);

        assertThatThrownBy(() -> userService.updateCurrentUser(nonExistentId, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("not found");
    }

    // ==========================================
    // Spring Security UserDetailsService Scenarios
    // ==========================================

    @Test
    @DisplayName("loadUserByUsername - should load UserDetails by email with user id as username")
    void loadUserByUsername_existingEmail_shouldReturnUserDetails() {
        User user = persistUser("Karen", "Page", "karen.page@example.com", VALID_IBAN_1, null);

        UserDetails userDetails = userService.loadUserByUsername(user.getEmail());

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(user.getId().toString());
        assertThat(userDetails.getPassword()).isEqualTo(user.getPassword());
    }

    @Test
    @DisplayName("loadUserByUsername - should throw UsernameNotFoundException when email not found")
    void loadUserByUsername_nonExistentEmail_shouldThrowUsernameNotFoundException() {
        assertThatThrownBy(() -> userService.loadUserByUsername("nonexistent@example.com"))
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessageContaining("User not found with email: nonexistent@example.com");
    }

    @Test
    @DisplayName("loadUserById - should load UserDetails by user id")
    void loadUserById_existingId_shouldReturnUserDetails() {
        User user = persistUser("Leo", "Fitz", "leo.fitz@example.com", VALID_IBAN_1, null);

        UserDetails userDetails = userService.loadUserById(user.getId());

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(user.getId().toString());
    }

    @Test
    @DisplayName("loadUserById - should throw UsernameNotFoundException when user id not found")
    void loadUserById_nonExistentId_shouldThrowUsernameNotFoundException() {
        UUID nonExistentId = UUID.randomUUID();

        assertThatThrownBy(() -> userService.loadUserById(nonExistentId))
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessageContaining("User not found with ID: " + nonExistentId);
    }

    @Test
    @DisplayName("loadUserById - should throw IllegalArgumentException when user id is null")
    void loadUserById_nullId_shouldThrowIllegalArgumentException() {
        assertThatThrownBy(() -> userService.loadUserById(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User ID must not be null");
    }

    // ==========================================
    // Unavailability DateRange Skeletons (Future feature)
    // ==========================================

    @Test
    @Disabled("TODO: unavailabilities are an upcoming feature")
    @DisplayName("shouldAddAndQueryUserUnavailabilityDateRanges verifies adding and querying unavailabilities for a user")
    void shouldAddAndQueryUserUnavailabilityDateRanges() {
        User user = persistUser("Eve", "Vacationer", "eve.vacationer@test.com", null, null);
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(7);
        DateRange dateRange = new DateRange(start, end);

        assertThat(user).isNotNull();
        assertThat(dateRange.startDate()).isEqualTo(start);
        assertThat(dateRange.endDate()).isEqualTo(end);
    }

    @Test
    @Disabled("TODO: unavailabilities are an upcoming feature")
    @DisplayName("shouldValidateUnavailabilityDateRanges verifies date range constraints")
    void shouldValidateUnavailabilityDateRanges() {
        LocalDateTime start = LocalDateTime.now().plusDays(7);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        assertThatThrownBy(() -> new DateRange(start, end))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Start date must be before end date");
    }

    @Test
    @Disabled("TODO: unavailabilities are an upcoming feature")
    @DisplayName("shouldQueryHouseholdUnavailabilities queries unavailability across household members")
    void shouldQueryHouseholdUnavailabilities() {
        User user = persistUser("Frank", "Traveler", "frank.traveler@test.com", null, null);
        assertThat(user).isNotNull();
    }

    // ==========================================
    // Helper Methods
    // ==========================================

    private User persistUser(String name, String surname, String email, String iban, String paymentLink) {
        User user = new User(name, surname, email, "encodedPassword123!");
        user.setIban(iban);
        user.setPaymentLink(paymentLink);
        return userRepository.saveAndFlush(user);
    }
}
