package com.housemate.backend.service;

import com.housemate.backend.model.user.User;
import com.housemate.backend.repository.user.UserRepository;
import com.housemate.shared.dto.user.request.UserUpdateRequestDTO;
import com.housemate.shared.dto.user.response.UserResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private static final UUID TEST_USER_ID = UUID.fromString("10000000-0000-0000-0000-000000000031");
    private static final String TEST_NAME = "Mario";
    private static final String TEST_SURNAME = "Rossi";
    private static final String TEST_EMAIL = "mario.rossi@example.com";
    private static final String TEST_EMAIL_2 = "luigi.verdi@example.com";
    private static final String TEST_PASSWORD = "encoded-password";
    private static final String TEST_IBAN = "IT60X0542811101000000123456";
    private static final String TEST_PAYMENT_LINK = "https://payments.example.com/mario";

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User(TEST_NAME, TEST_SURNAME, TEST_EMAIL, TEST_PASSWORD);
        ReflectionTestUtils.setField(testUser, "id", TEST_USER_ID);
        testUser.setIban(TEST_IBAN);
        testUser.setPaymentLink(TEST_PAYMENT_LINK);
    }

    // ============ Tests for loadUserByUsername ============

    @Test
    @DisplayName("loadUserByUsername - should return UserDetails when email exists")
    void loadUserByUsername_existingEmail_returnsUserDetails() {
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        UserDetails result = userService.loadUserByUsername(TEST_EMAIL);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(TEST_USER_ID.toString());
        assertThat(result.getPassword()).isEqualTo(TEST_PASSWORD);
        verify(userRepository).findByEmail(TEST_EMAIL);
    }

    @Test
    @DisplayName("loadUserByUsername - should throw UsernameNotFoundException when email does not exist")
    void loadUserByUsername_missingEmail_throwsUsernameNotFoundException() {
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.loadUserByUsername(TEST_EMAIL))
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessage("User not found with email: " + TEST_EMAIL);

        verify(userRepository).findByEmail(TEST_EMAIL);
    }

    // ============ Tests for loadUserById ============

    @Test
    @DisplayName("loadUserById - should throw IllegalArgumentException when userId is null")
    void loadUserById_nullUserId_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> userService.loadUserById(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("User ID must not be null");

        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("loadUserById - should throw UsernameNotFoundException when user does not exist")
    void loadUserById_missingUser_throwsUsernameNotFoundException() {
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.loadUserById(TEST_USER_ID))
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessage("User not found with ID: " + TEST_USER_ID);

        verify(userRepository).findById(TEST_USER_ID);
    }

    @Test
    @DisplayName("loadUserById - should return UserDetails when user exists")
    void loadUserById_existingUser_returnsUserDetails() {
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

        UserDetails result = userService.loadUserById(TEST_USER_ID);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(TEST_USER_ID.toString());
        assertThat(result.getPassword()).isEmpty();
        verify(userRepository).findById(TEST_USER_ID);
    }

    // ============ Tests for getCurrentUser ============

    @Test
    @DisplayName("getCurrentUser - should throw IllegalArgumentException when userId is null")
    void getCurrentUser_nullUserId_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> userService.getCurrentUser(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("User ID must not be null");

        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("getCurrentUser - should throw IllegalArgumentException when user is not found")
    void getCurrentUser_missingUser_throwsIllegalArgumentException() {
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getCurrentUser(TEST_USER_ID))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("User with ID: " + TEST_USER_ID + " not found.");

        verify(userRepository).findById(TEST_USER_ID);
    }

    @Test
    @DisplayName("getCurrentUser - should return UserResponseDTO when user exists")
    void getCurrentUser_existingUser_returnsUserResponseDTO() {
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

        UserResponseDTO response = userService.getCurrentUser(TEST_USER_ID);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(TEST_USER_ID);
        assertThat(response.email()).isEqualTo(TEST_EMAIL);
        assertThat(response.iban()).isEqualTo(TEST_IBAN);
        assertThat(response.paymentLink()).isEqualTo(TEST_PAYMENT_LINK);
        verify(userRepository).findById(TEST_USER_ID);
    }

    // ============ Tests for updateCurrentUser ============

    @Test
    @DisplayName("updateCurrentUser - should throw IllegalArgumentException when userId is null")
    void updateCurrentUser_nullUserId_throwsIllegalArgumentException() {
        UserUpdateRequestDTO dto = new UserUpdateRequestDTO(TEST_NAME, TEST_SURNAME, TEST_EMAIL, TEST_IBAN, TEST_PAYMENT_LINK);

        assertThatThrownBy(() -> userService.updateCurrentUser(null, dto))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("User ID must not be null");

        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("updateCurrentUser - should throw IllegalArgumentException when dto is null")
    void updateCurrentUser_nullDto_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> userService.updateCurrentUser(TEST_USER_ID, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("No request body was sent");

        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("updateCurrentUser - should throw IllegalArgumentException when user is not found")
    void updateCurrentUser_missingUser_throwsIllegalArgumentException() {
        UserUpdateRequestDTO dto = new UserUpdateRequestDTO(TEST_NAME, TEST_SURNAME, TEST_EMAIL, TEST_IBAN, TEST_PAYMENT_LINK);
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateCurrentUser(TEST_USER_ID, dto))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("User with ID: " + TEST_USER_ID + " not found.");

        verify(userRepository).findById(TEST_USER_ID);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("updateCurrentUser - should return current user without save when no fields are provided")
    void updateCurrentUser_noFieldsProvided_returnsCurrentUserWithoutSaving() {
        UserUpdateRequestDTO dto = new UserUpdateRequestDTO(null, null, null, null, null);
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

        UserResponseDTO response = userService.updateCurrentUser(TEST_USER_ID, dto);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(TEST_USER_ID);
        assertThat(response.email()).isEqualTo(TEST_EMAIL);
        verify(userRepository).findById(TEST_USER_ID);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("updateCurrentUser - should throw IllegalArgumentException when email format is invalid")
    void updateCurrentUser_invalidEmail_throwsIllegalArgumentException() {
        UserUpdateRequestDTO dto = new UserUpdateRequestDTO(null, null, "not-an-email", null, null);
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> userService.updateCurrentUser(TEST_USER_ID, dto))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Email must be a valid email address");

        verify(userRepository).findById(TEST_USER_ID);
        verify(userRepository, never()).existsByEmailAndIdNot(anyString(), any(UUID.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("updateCurrentUser - should throw IllegalArgumentException when email is already registered")
    void updateCurrentUser_duplicateEmail_throwsIllegalArgumentException() {
        UserUpdateRequestDTO dto = new UserUpdateRequestDTO(null, null, TEST_EMAIL_2, null, null);
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmailAndIdNot(TEST_EMAIL_2, TEST_USER_ID)).thenReturn(true);

        assertThatThrownBy(() -> userService.updateCurrentUser(TEST_USER_ID, dto))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Email already registered");

        verify(userRepository).findById(TEST_USER_ID);
        verify(userRepository).existsByEmailAndIdNot(TEST_EMAIL_2, TEST_USER_ID);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("updateCurrentUser - should skip email uniqueness check when email is unchanged")
    void updateCurrentUser_sameEmail_skipsUniquenessCheck() {
        UserUpdateRequestDTO dto = new UserUpdateRequestDTO(null, null, TEST_EMAIL, null, null);
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseDTO response = userService.updateCurrentUser(TEST_USER_ID, dto);

        assertThat(response.email()).isEqualTo(TEST_EMAIL);
        verify(userRepository).findById(TEST_USER_ID);
        verify(userRepository, never()).existsByEmailAndIdNot(anyString(), any(UUID.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("updateCurrentUser - should throw IllegalArgumentException when IBAN format is invalid")
    void updateCurrentUser_invalidIban_throwsIllegalArgumentException() {
        UserUpdateRequestDTO dto = new UserUpdateRequestDTO(null, null, null, "invalid-iban", null);
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> userService.updateCurrentUser(TEST_USER_ID, dto))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("IBAN must be a valid IBAN format");

        verify(userRepository).findById(TEST_USER_ID);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("updateCurrentUser - should throw IllegalArgumentException when payment link format is invalid")
    void updateCurrentUser_invalidPaymentLink_throwsIllegalArgumentException() {
        UserUpdateRequestDTO dto = new UserUpdateRequestDTO(null, null, null, null, "https://bad host.com/pay");
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> userService.updateCurrentUser(TEST_USER_ID, dto))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Payment link must be a valid URL starting with http:// or https://");

        verify(userRepository).findById(TEST_USER_ID);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("updateCurrentUser - should update selected fields and persist user")
    void updateCurrentUser_partialUpdate_persistsChanges() {
        UserUpdateRequestDTO dto = new UserUpdateRequestDTO(
            "Giulia",
            null,
            TEST_EMAIL_2,
            null,
            "https://payments.example.com/giulia"
        );
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmailAndIdNot(TEST_EMAIL_2, TEST_USER_ID)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseDTO response = userService.updateCurrentUser(TEST_USER_ID, dto);

        assertThat(response.name()).isEqualTo("Giulia");
        assertThat(response.surname()).isEqualTo(TEST_SURNAME);
        assertThat(response.email()).isEqualTo(TEST_EMAIL_2);
        assertThat(response.paymentLink()).isEqualTo("https://payments.example.com/giulia");

        verify(userRepository).findById(TEST_USER_ID);
        verify(userRepository).existsByEmailAndIdNot(TEST_EMAIL_2, TEST_USER_ID);
        verify(userRepository).save(any(User.class));
    }

    // ============ Tests for toUserResponseDTO ============

    @Test
    @DisplayName("toUserResponseDTO - should throw IllegalArgumentException when user is null")
    void toUserResponseDTO_nullUser_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> userService.toUserResponseDTO(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("User must not be null");
    }

    @Test
    @DisplayName("toUserResponseDTO - should map user fields correctly")
    void toUserResponseDTO_validUser_mapsFieldsCorrectly() {
        UserResponseDTO dto = userService.toUserResponseDTO(testUser);

        assertThat(dto.id()).isEqualTo(TEST_USER_ID);
        assertThat(dto.name()).isEqualTo(TEST_NAME);
        assertThat(dto.surname()).isEqualTo(TEST_SURNAME);
        assertThat(dto.email()).isEqualTo(TEST_EMAIL);
        assertThat(dto.iban()).isEqualTo(TEST_IBAN);
        assertThat(dto.paymentLink()).isEqualTo(TEST_PAYMENT_LINK);
    }

}