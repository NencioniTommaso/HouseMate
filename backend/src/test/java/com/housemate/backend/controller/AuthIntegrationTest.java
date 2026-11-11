package com.housemate.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.housemate.backend.model.user.User;
import com.housemate.backend.repository.user.UserRepository;
import com.housemate.backend.service.JwtService;
import com.housemate.shared.dto.auth.request.LoginRequestDTO;
import com.housemate.shared.dto.auth.request.RegisterRequestDTO;
import com.housemate.shared.dto.auth.response.LoginResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Auth and Security Filter Chain Integration Tests")
@SuppressWarnings("null")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private static final String DEFAULT_INVITE_CODE = "dev-secret-code";

    @Test
    @DisplayName("Scenario 1: Register new user via POST /api/auth/register persists in DB and returns 201 Created")
    void registerNewUser_persistsInDbAndReturnsCreated() throws Exception {
        RegisterRequestDTO registerRequest = new RegisterRequestDTO(
                "Marco",
                "Bianchi",
                "marco.bianchi@example.com",
                "Password123!",
                "IT60X0542811101000000123456",
                DEFAULT_INVITE_CODE
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.user.email").value("marco.bianchi@example.com"))
                .andExpect(jsonPath("$.user.name").value("Marco"))
                .andExpect(jsonPath("$.user.surname").value("Bianchi"))
                .andExpect(jsonPath("$.user.iban").value("IT60X0542811101000000123456"));

        Optional<User> savedUserOptional = userRepository.findByEmail("marco.bianchi@example.com");
        assertThat(savedUserOptional).isPresent();

        User savedUser = savedUserOptional.get();
        assertThat(savedUser.getName()).isEqualTo("Marco");
        assertThat(savedUser.getSurname()).isEqualTo("Bianchi");
        assertThat(savedUser.getIban()).isEqualTo("IT60X0542811101000000123456");
        assertThat(passwordEncoder.matches("Password123!", savedUser.getPassword())).isTrue();
    }

    @Test
    @DisplayName("Scenario 1b: Register with invalid invite code returns 400 Bad Request")
    void registerWithInvalidInviteCode_returnsBadRequest() throws Exception {
        RegisterRequestDTO registerRequest = new RegisterRequestDTO(
                "Marco",
                "Bianchi",
                "marco.badinvite@example.com",
                "Password123!",
                null,
                "wrong-code"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());

        assertThat(userRepository.findByEmail("marco.badinvite@example.com")).isEmpty();
    }

    @Test
    @DisplayName("Scenario 2: Login with valid credentials returns 200 OK and valid JWT matching user info")
    void loginWithValidCredentials_returnsOkAndValidJwt() throws Exception {
        User user = new User("Giulia", "Verdi", "giulia.verdi@example.com", passwordEncoder.encode("Password123!"));
        user = userRepository.save(user);

        LoginRequestDTO loginRequest = new LoginRequestDTO("giulia.verdi@example.com", "Password123!");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.user.email").value("giulia.verdi@example.com"))
                .andExpect(jsonPath("$.user.name").value("Giulia"))
                .andExpect(jsonPath("$.user.surname").value("Verdi"))
                .andReturn();

        LoginResponseDTO loginResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                LoginResponseDTO.class
        );

        assertThat(loginResponse.token()).isNotBlank();
        String subject = jwtService.extractSubject(loginResponse.token());
        assertThat(subject).isEqualTo(user.getId().toString());
    }

    @Test
    @DisplayName("Scenario 2b: Login with invalid password returns 401 Unauthorized")
    void loginWithInvalidPassword_returnsUnauthorized() throws Exception {
        User user = new User("Giulia", "Verdi", "giulia.invalid@example.com", passwordEncoder.encode("Password123!"));
        userRepository.save(user);

        LoginRequestDTO loginRequest = new LoginRequestDTO("giulia.invalid@example.com", "WrongPassword!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Scenario 3: Access protected endpoint /api/users/me with valid Bearer token returns 200 OK")
    void accessProtectedEndpoint_withValidBearerToken_returnsOk() throws Exception {
        User user = new User("Luca", "Neri", "luca.neri@example.com", passwordEncoder.encode("Password123!"));
        user = userRepository.save(user);

        org.springframework.security.core.userdetails.UserDetails userDetails =
                org.springframework.security.core.userdetails.User.builder()
                        .username(user.getId().toString())
                        .password("")
                        .build();
        String token = jwtService.generateToken(userDetails);

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId().toString()))
                .andExpect(jsonPath("$.email").value("luca.neri@example.com"))
                .andExpect(jsonPath("$.name").value("Luca"))
                .andExpect(jsonPath("$.surname").value("Neri"));
    }

    @Test
    @DisplayName("Scenario 4a: Access protected endpoint with missing Authorization header returns 401 Unauthorized")
    void accessProtectedEndpoint_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Scenario 4b: Access protected endpoint with malformed JWT returns 401 Unauthorized")
    void accessProtectedEndpoint_withMalformedToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer malformed.invalid.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Scenario 4c: Access protected endpoint with tampered JWT signature returns 401 Unauthorized")
    void accessProtectedEndpoint_withTamperedToken_returnsUnauthorized() throws Exception {
        User user = new User("Tamper", "Test", "tamper.test@example.com", passwordEncoder.encode("Password123!"));
        user = userRepository.save(user);

        org.springframework.security.core.userdetails.UserDetails userDetails =
                org.springframework.security.core.userdetails.User.builder()
                        .username(user.getId().toString())
                        .password("")
                        .build();
        String validToken = jwtService.generateToken(userDetails);
        String tamperedToken = validToken.substring(0, validToken.length() - 5) + "abcde";

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + tamperedToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Scenario 4d: Access protected endpoint with valid JWT for non-existent user returns 401 Unauthorized")
    void accessProtectedEndpoint_withNonExistentUserToken_returnsUnauthorized() throws Exception {
        UUID nonExistentUserId = UUID.randomUUID();
        org.springframework.security.core.userdetails.UserDetails userDetails =
                org.springframework.security.core.userdetails.User.builder()
                        .username(nonExistentUserId.toString())
                        .password("")
                        .build();
        String token = jwtService.generateToken(userDetails);

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }
}
