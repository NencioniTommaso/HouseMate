package com.housemate.backend.service;

import com.housemate.backend.model.user.User;
import com.housemate.backend.repository.user.UserRepository;
import com.housemate.backend.service.utils.UserValidationUtils;
import com.housemate.shared.dto.user.request.UserUpdateRequestDTO;
import com.housemate.shared.dto.user.response.UserResponseDTO;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    // Used for login authentication by Spring Security
    @Override
    @NonNull
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return Objects.requireNonNull(
            org.springframework.security.core.userdetails.User.builder()
                .username(user.getId().toString()) 
                .password(user.getPassword())
                .build(),
            "unexpectedly built a null UserDetails object for email: " + email
        );
    }

    // Used for JWT authentication, password is not needed as validation is ID-based
    @NonNull
    public UserDetails loadUserById(@NonNull UUID id) throws UsernameNotFoundException, IllegalArgumentException {
        Assert.notNull(id, "User ID must not be null");
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + id));

        return Objects.requireNonNull(
            org.springframework.security.core.userdetails.User.builder()
                    .username(user.getId().toString())
                    .password("")
                    .build(),
            "unexpectedly built a null UserDetails object for user ID: " + id
        );
    }

    @Transactional(readOnly = true)
    @NonNull
    public UserResponseDTO getCurrentUser(@NonNull UUID userId) {
        Assert.notNull(userId, "User ID must not be null");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID: " + userId + " not found."));

        return toUserResponseDTO(
            Objects.requireNonNull(
                user, "Unexpectedly null user found with ID: " + userId
            )
        );
    }

    @Transactional
    @NonNull
    public UserResponseDTO updateCurrentUser(@NonNull UUID userId, @NonNull UserUpdateRequestDTO dto) {
        Assert.notNull(userId, "User ID must not be null");
        Assert.notNull(dto, "No request body was sent");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID: " + userId + " not found."));

        boolean hasChanges = false;

        if (dto.name() != null) {
            Assert.isTrue(!dto.name().isBlank(), "Name cannot be blank");
            user.setName(dto.name());
            hasChanges = true;
        }

        if (dto.surname() != null) {
            Assert.isTrue(!dto.surname().isBlank(), "Surname cannot be blank");
            user.setSurname(dto.surname());
            hasChanges = true;
        }

        if (dto.email() != null) {
            Assert.isTrue(!dto.email().isBlank(), "Email cannot be blank");
            Assert.isTrue(UserValidationUtils.isValidEmail(dto.email()), "Email must be a valid email address");

            if (!user.getEmail().equals(dto.email()) && userRepository.existsByEmailAndIdNot(dto.email(), userId)) {
                throw new IllegalArgumentException("Email already registered");
            }

            user.setEmail(dto.email());
            hasChanges = true;
        }

        if (dto.iban() != null) {
            Assert.isTrue(!dto.iban().isBlank(), "IBAN cannot be blank");
            Assert.isTrue(UserValidationUtils.isValidIban(dto.iban()), "IBAN must be a valid IBAN format");
            user.setIban(dto.iban());
            hasChanges = true;
        }

        if (dto.paymentLink() != null) {
            Assert.isTrue(!dto.paymentLink().isBlank(), "Payment link cannot be blank");
            Assert.isTrue(
                UserValidationUtils.isValidPaymentLink(dto.paymentLink()),
                "Payment link must be a valid URL starting with http:// or https://"
            );
            user.setPaymentLink(dto.paymentLink());
            hasChanges = true;
        }

        if (!hasChanges) {
            return toUserResponseDTO(user);
        }

        User savedUser = userRepository.save(user);
        return toUserResponseDTO(savedUser);
    }

    @NonNull
    public UserResponseDTO toUserResponseDTO(@NonNull User user) {
		Assert.notNull(user, "User must not be null");
        return new UserResponseDTO(
			user.getId(),
			user.getName(),
			user.getSurname(),
			user.getEmail(),
			user.getIban(),
			user.getPaymentLink()
		);
	}
}
