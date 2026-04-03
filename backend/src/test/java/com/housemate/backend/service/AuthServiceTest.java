package com.housemate.backend.service;

import com.housemate.backend.model.user.User;
import com.housemate.backend.repository.user.UserRepository;
import com.housemate.shared.dto.auth.request.LoginRequestDTO;
import com.housemate.shared.dto.auth.request.RegisterRequestDTO;
import com.housemate.shared.dto.auth.response.LoginResponseDTO;
import com.housemate.shared.dto.user.response.UserResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
@SuppressWarnings("null")
class AuthServiceTest {

    // ============ Test Data Constants ============
    private static final UUID TEST_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final String TEST_NAME = "Mario";
    private static final String TEST_SURNAME = "Rossi";
    private static final String TEST_EMAIL = "mario@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_IBAN = "IT60X0542811101000000123456";
    private static final String TEST_ENCODED_PASSWORD = "encoded-password";
    private static final String TEST_TOKEN = "jwt-token";

    // ============ Test Objects ============
    private LoginRequestDTO testLoginRequestDTO;
    private RegisterRequestDTO testRegisterRequestDTO;
    private RegisterRequestDTO testRegisterRequestDTONullIban;
    private UserResponseDTO testUserResponseDTO;
    private UserResponseDTO testUserResponseDTONullIban;
    private User testSavedUser;
    private User testSavedUserNullIban;

    // ============ Mock Dependencies ============
    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private Authentication authentication;

    // ============ Service Under Test ============
    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        testLoginRequestDTO = new LoginRequestDTO(TEST_EMAIL, TEST_PASSWORD);
        testRegisterRequestDTO = new RegisterRequestDTO(TEST_NAME, TEST_SURNAME, TEST_EMAIL, TEST_PASSWORD, TEST_IBAN);
        testRegisterRequestDTONullIban = new RegisterRequestDTO(TEST_NAME, TEST_SURNAME, TEST_EMAIL, TEST_PASSWORD, null);

        testUserResponseDTO = new UserResponseDTO(
            TEST_USER_ID,
            TEST_NAME,
            TEST_SURNAME,
            TEST_EMAIL,
            TEST_IBAN,
            null
        );
        testUserResponseDTONullIban = new UserResponseDTO(
            TEST_USER_ID,
            TEST_NAME,
            TEST_SURNAME,
            TEST_EMAIL,
            null,
            null
        );

        testSavedUser = new User(TEST_NAME, TEST_SURNAME, TEST_EMAIL, TEST_ENCODED_PASSWORD);
        testSavedUser.setId(TEST_USER_ID);
        testSavedUser.setIban(TEST_IBAN);

        testSavedUserNullIban = new User(TEST_NAME, TEST_SURNAME, TEST_EMAIL, TEST_ENCODED_PASSWORD);
        testSavedUserNullIban.setId(TEST_USER_ID);
        testSavedUserNullIban.setIban(null);
    }


    // ============ Login Tests ============

    @Test
    @DisplayName("login - should return LoginResponseDTO on valid credentials")
    void login_validCredentials_returnsLoginResponse() {
        LoginRequestDTO dto = testLoginRequestDTO;
        UserDetails principal = org.springframework.security.core.userdetails.User.builder()
            .username(TEST_USER_ID.toString())
            .password("ignored")
            .build();

        User user = testSavedUser;
        UserResponseDTO userResponseDTO = testUserResponseDTO;

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(jwtService.generateToken(principal)).thenReturn(TEST_TOKEN);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
        when(userService.toUserResponseDTO(user)).thenReturn(userResponseDTO);

        LoginResponseDTO response = authService.login(dto);

        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo(TEST_TOKEN);
        assertThat(response.user()).isEqualTo(userResponseDTO);

        ArgumentCaptor<UsernamePasswordAuthenticationToken> authTokenCaptor = ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(authTokenCaptor.capture());
        assertThat(authTokenCaptor.getValue().getPrincipal()).isEqualTo(TEST_EMAIL);
        assertThat(authTokenCaptor.getValue().getCredentials()).isEqualTo(TEST_PASSWORD);

        verify(jwtService).generateToken(principal);
        verify(userRepository).findByEmail(TEST_EMAIL);
        verify(userService).toUserResponseDTO(user);
    }

    @Test
    @DisplayName("login - should throw IllegalArgumentException when DTO is null")
    void login_nullDto_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> authService.login(null))
            .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(authenticationManager, jwtService, userRepository, userService);
    }

    @Test
    @DisplayName("login - should throw IllegalArgumentException when email is null")
    void login_nullEmail_throwsIllegalArgumentException() {
        LoginRequestDTO dto = new LoginRequestDTO(null, TEST_PASSWORD);

        assertThatThrownBy(() -> authService.login(dto))
            .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(authenticationManager, jwtService, userRepository, userService);
    }

    @Test
    @DisplayName("login - should throw IllegalArgumentException when email is blank")
    void login_blankEmail_throwsIllegalArgumentException() {
        LoginRequestDTO dto = new LoginRequestDTO("   ", TEST_PASSWORD);

        assertThatThrownBy(() -> authService.login(dto))
            .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(authenticationManager, jwtService, userRepository, userService);
    }

    @Test
    @DisplayName("login - should throw IllegalArgumentException when email is invalid")
    void login_invalidEmail_throwsIllegalArgumentException() {
        LoginRequestDTO dto = new LoginRequestDTO("not-an-email", TEST_PASSWORD);

        assertThatThrownBy(() -> authService.login(dto))
            .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(authenticationManager, jwtService, userRepository, userService);
    }

    @Test
    @DisplayName("login - should throw IllegalArgumentException when password is null")
    void login_nullPassword_throwsIllegalArgumentException() {
        LoginRequestDTO dto = new LoginRequestDTO(TEST_EMAIL, null);

        assertThatThrownBy(() -> authService.login(dto))
            .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(authenticationManager, jwtService, userRepository, userService);
    }

    @Test
    @DisplayName("login - should throw IllegalArgumentException when password is blank")
    void login_blankPassword_throwsIllegalArgumentException() {
        LoginRequestDTO dto = new LoginRequestDTO(TEST_EMAIL, "   ");

        assertThatThrownBy(() -> authService.login(dto))
            .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(authenticationManager, jwtService, userRepository, userService);
    }

    @Test
    @DisplayName("login - should throw NullPointerException when authentication principal is null")
    void login_nullPrincipalAfterAuthentication_throwsNullPointerException() {
        LoginRequestDTO dto = testLoginRequestDTO;

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(null);

        assertThatThrownBy(() -> authService.login(dto))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Unexpectedly null principal in Authentication object after successful authentication");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(authentication).getPrincipal();
        verifyNoInteractions(jwtService, userRepository, userService);
    }

    @Test
    @DisplayName("login - should throw IllegalArgumentException when user is missing after successful authentication")
    void login_userMissingAfterSuccessfulAuthentication_throwsIllegalArgumentException() {
        LoginRequestDTO dto = testLoginRequestDTO;
        UserDetails principal = org.springframework.security.core.userdetails.User.builder()
            .username(TEST_USER_ID.toString())
            .password("ignored")
            .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(jwtService.generateToken(principal)).thenReturn(TEST_TOKEN);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(dto))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("User not found after successful authentication");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(principal);
        verify(userRepository).findByEmail(TEST_EMAIL);
        verifyNoInteractions(userService);
    }

    // ============ Register Tests ============

    @Test
    @DisplayName("register - should return LoginResponseDTO and persist encoded password on valid input")
    void register_validInput_returnsLoginResponseAndPersistsEncodedPassword() {
        RegisterRequestDTO dto = testRegisterRequestDTO;
        User savedUser = testSavedUser;
        UserResponseDTO userResponseDTO = testUserResponseDTO;

        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(TEST_ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn(TEST_TOKEN);
        when(userService.toUserResponseDTO(savedUser)).thenReturn(userResponseDTO);

        LoginResponseDTO response = authService.register(dto);

        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo(TEST_TOKEN);
        assertThat(response.user()).isEqualTo(userResponseDTO);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User persistedUser = userCaptor.getValue();

        assertThat(persistedUser.getName()).isEqualTo(TEST_NAME);
        assertThat(persistedUser.getSurname()).isEqualTo(TEST_SURNAME);
        assertThat(persistedUser.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(persistedUser.getPassword()).isEqualTo(TEST_ENCODED_PASSWORD);
        assertThat(persistedUser.getIban()).isEqualTo(TEST_IBAN);

        ArgumentCaptor<UserDetails> userDetailsCaptor = ArgumentCaptor.forClass(UserDetails.class);
        verify(jwtService).generateToken(userDetailsCaptor.capture());
        assertThat(userDetailsCaptor.getValue().getUsername()).isEqualTo(TEST_USER_ID.toString());
        verify(userService).toUserResponseDTO(savedUser);

        verify(passwordEncoder).encode(TEST_PASSWORD);
        verify(userRepository).existsByEmail(TEST_EMAIL);
    }

    @Test
    @DisplayName("register - should accept null IBAN and persist null IBAN")
    void register_nullIban_isAcceptedAndPersistsNullIban() {
        RegisterRequestDTO dto = testRegisterRequestDTONullIban;
        User savedUser = testSavedUserNullIban;
        UserResponseDTO userResponseDTO = testUserResponseDTONullIban;

        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(TEST_ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn(TEST_TOKEN);
        when(userService.toUserResponseDTO(savedUser)).thenReturn(userResponseDTO);

        LoginResponseDTO response = authService.register(dto);

        assertThat(response.token()).isEqualTo(TEST_TOKEN);
        assertThat(response.user()).isEqualTo(userResponseDTO);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getIban()).isNull();
    }

    @Test
    @DisplayName("register - should throw IllegalArgumentException when DTO is null")
    void register_nullDto_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> authService.register(null))
            .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(userRepository, passwordEncoder, jwtService, userService);
    }

    @Test
    @DisplayName("register - should throw IllegalArgumentException when email is null")
    void register_nullEmail_throwsIllegalArgumentException() {
        RegisterRequestDTO dto = new RegisterRequestDTO(TEST_NAME, TEST_SURNAME, null, TEST_PASSWORD, TEST_IBAN);

        assertThatThrownBy(() -> authService.register(dto))
            .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(userRepository, passwordEncoder, jwtService, userService);
    }

    @Test
    @DisplayName("register - should throw IllegalArgumentException when email is blank")
    void register_blankEmail_throwsIllegalArgumentException() {
        RegisterRequestDTO dto = new RegisterRequestDTO(TEST_NAME, TEST_SURNAME, "   ", TEST_PASSWORD, TEST_IBAN);

        assertThatThrownBy(() -> authService.register(dto))
            .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(userRepository, passwordEncoder, jwtService, userService);
    }

    @Test
    @DisplayName("register - should throw IllegalArgumentException when email is invalid")
    void register_invalidEmail_throwsIllegalArgumentException() {
        RegisterRequestDTO dto = new RegisterRequestDTO(TEST_NAME, TEST_SURNAME, "invalid-email", TEST_PASSWORD, TEST_IBAN);

        assertThatThrownBy(() -> authService.register(dto))
            .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(userRepository, passwordEncoder, jwtService, userService);
    }

    @Test
    @DisplayName("register - should throw IllegalArgumentException when password is null")
    void register_nullPassword_throwsIllegalArgumentException() {
        RegisterRequestDTO dto = new RegisterRequestDTO(TEST_NAME, TEST_SURNAME, TEST_EMAIL, null, TEST_IBAN);

        assertThatThrownBy(() -> authService.register(dto))
            .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(userRepository, passwordEncoder, jwtService, userService);
    }

    @Test
    @DisplayName("register - should throw IllegalArgumentException when password is blank")
    void register_blankPassword_throwsIllegalArgumentException() {
        RegisterRequestDTO dto = new RegisterRequestDTO(TEST_NAME, TEST_SURNAME, TEST_EMAIL, "   ", TEST_IBAN);

        assertThatThrownBy(() -> authService.register(dto))
            .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(userRepository, passwordEncoder, jwtService, userService);
    }

    @Test
    @DisplayName("register - should throw IllegalArgumentException when name is null")
    void register_nullName_throwsIllegalArgumentException() {
        RegisterRequestDTO dto = new RegisterRequestDTO(null, TEST_SURNAME, TEST_EMAIL, TEST_PASSWORD, TEST_IBAN);

        assertThatThrownBy(() -> authService.register(dto))
            .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(userRepository, passwordEncoder, jwtService, userService);
    }

    @Test
    @DisplayName("register - should throw IllegalArgumentException when name is blank")
    void register_blankName_throwsIllegalArgumentException() {
        RegisterRequestDTO dto = new RegisterRequestDTO("   ", TEST_SURNAME, TEST_EMAIL, TEST_PASSWORD, TEST_IBAN);

        assertThatThrownBy(() -> authService.register(dto))
            .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(userRepository, passwordEncoder, jwtService, userService);
    }

    @Test
    @DisplayName("register - should throw IllegalArgumentException when surname is null")
    void register_nullSurname_throwsIllegalArgumentException() {
        RegisterRequestDTO dto = new RegisterRequestDTO(TEST_NAME, null, TEST_EMAIL, TEST_PASSWORD, TEST_IBAN);

        assertThatThrownBy(() -> authService.register(dto))
            .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(userRepository, passwordEncoder, jwtService, userService);
    }

    @Test
    @DisplayName("register - should throw IllegalArgumentException when surname is blank")
    void register_blankSurname_throwsIllegalArgumentException() {
        RegisterRequestDTO dto = new RegisterRequestDTO(TEST_NAME, "   ", TEST_EMAIL, TEST_PASSWORD, TEST_IBAN);

        assertThatThrownBy(() -> authService.register(dto))
            .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(userRepository, passwordEncoder, jwtService, userService);
    }

    @Test
    @DisplayName("register - should throw IllegalArgumentException when IBAN is invalid")
    void register_invalidIban_throwsIllegalArgumentException() {
        RegisterRequestDTO dto = new RegisterRequestDTO(TEST_NAME, TEST_SURNAME, TEST_EMAIL, TEST_PASSWORD, "not-a-valid-iban");

        assertThatThrownBy(() -> authService.register(dto))
            .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(userRepository, passwordEncoder, jwtService, userService);
    }

    @Test
    @DisplayName("register - should throw IllegalArgumentException when email is already registered")
    void register_emailAlreadyRegistered_throwsIllegalArgumentException() {
        RegisterRequestDTO dto = testRegisterRequestDTO;

        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> authService.register(dto))
            .isInstanceOf(IllegalArgumentException.class);

        verify(userRepository).existsByEmail(TEST_EMAIL);
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(jwtService, userService);
    }
}