package com.housemate.backend.service;

import com.housemate.backend.model.user.User;
import com.housemate.backend.repository.user.UserRepository;
import com.housemate.backend.service.utils.UserValidationUtils;
import com.housemate.shared.dto.auth.request.LoginRequestDTO;
import com.housemate.shared.dto.auth.request.RegisterRequestDTO;
import com.housemate.shared.dto.auth.response.LoginResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserService userService;
	private final UserRepository userRepository;
	private final AuthenticationManager authenticationManager;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	@Transactional(readOnly = true)
	public LoginResponseDTO login(@NonNull LoginRequestDTO dto) throws IllegalArgumentException, AuthenticationException {
        Assert.notNull(dto, "LoginRequestDTO must not be null");

        Assert.notNull(dto.email(), "Email in LoginRequestDTO must not be null");
        Assert.isTrue(!dto.email().isBlank(), "Email in LoginRequestDTO must not be blank");
		Assert.isTrue(UserValidationUtils.isValidEmail(dto.email()), "Email in LoginRequestDTO must be a valid email address");
        
        Assert.notNull(dto.password(), "Password in LoginRequestDTO must not be null");
        Assert.isTrue(!dto.password().isBlank(), "Password in LoginRequestDTO must not be blank");

		Authentication authentication = authenticationManager.authenticate(
			new UsernamePasswordAuthenticationToken(dto.email(), dto.password())
		);

		UserDetails userDetails = Objects.requireNonNull(
            (UserDetails) authentication.getPrincipal(),
            "Unexpectedly null principal in Authentication object after successful authentication"
        );
		String token = jwtService.generateToken(userDetails);

		User user = userRepository.findByEmail(dto.email())
			.orElseThrow(() -> new IllegalArgumentException("User not found after successful authentication"));

		log.info("User logged in successfully with principal: {}", userDetails.getUsername());
		return new LoginResponseDTO(
			userService.toUserResponseDTO(
				Objects.requireNonNull(user, "Unexpectedly null user after successful authentication")
			),
			token
		);
	}

	@Transactional
	public LoginResponseDTO register(@NonNull RegisterRequestDTO dto) throws IllegalArgumentException {
		Assert.notNull(dto, "RegisterRequestDTO must not be null");

		Assert.notNull(dto.email(), "Email in RegisterRequestDTO must not be null");
		Assert.isTrue(!dto.email().isBlank(), "Email in RegisterRequestDTO must not be blank");
		Assert.isTrue(UserValidationUtils.isValidEmail(dto.email()), "Email in RegisterRequestDTO must be a valid email address");

        Assert.notNull(dto.password(), "Password in RegisterRequestDTO must not be null");
        Assert.isTrue(!dto.password().isBlank(), "Password in RegisterRequestDTO must not be blank");

        Assert.notNull(dto.name(), "Name in RegisterRequestDTO must not be null");
        Assert.isTrue(!dto.name().isBlank(), "Name in RegisterRequestDTO must not be blank");

        Assert.notNull(dto.surname(), "Surname in RegisterRequestDTO must not be null");
        Assert.isTrue(!dto.surname().isBlank(), "Surname in RegisterRequestDTO must not be blank");

        Assert.isTrue(
			dto.iban() == null || UserValidationUtils.isValidIban(dto.iban()),
            "IBAN in RegisterRequestDTO must be a valid IBAN format"
        );

		if (userRepository.existsByEmail(dto.email())) {
			throw new IllegalArgumentException("Email already registered");
		}

		User user = new User(
			dto.name(),
			dto.surname(),
			dto.email(),
			passwordEncoder.encode(dto.password())
		);
		user.setIban(dto.iban());

		User savedUser = userRepository.save(user);
		UserDetails userDetails = Objects.requireNonNull(
            org.springframework.security.core.userdetails.User.builder()
                .username(savedUser.getId().toString())
                .password("")
                .build(),
            "Unexpectedly built a null UserDetails object for registered user with email: " + savedUser.getEmail()
        );
		String token = jwtService.generateToken(userDetails);

		log.info("User registered successfully with id: {}", savedUser.getId());
		return new LoginResponseDTO(userService.toUserResponseDTO(savedUser), token);
	}
}