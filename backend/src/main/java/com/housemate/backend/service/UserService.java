package com.housemate.backend.service;

import com.housemate.backend.model.user.User;
import com.housemate.backend.repository.user.UserRepository;
import com.housemate.shared.dto.user.response.UserResponseDTO;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.lang.NonNull;
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
                .authorities(java.util.Collections.emptyList())
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
                    .authorities(java.util.Collections.emptyList())
                    .build(),
            "unexpectedly built a null UserDetails object for user ID: " + id
        );
    }

    @NonNull
    public UserResponseDTO toUserResponseDTO(@NonNull User user) {
		Assert.notNull(user, "User must not be null");
        return new UserResponseDTO(
			user.getId(),
			user.getName(),
			user.getSurname(),
			user.getEmail(),
			user.getIban()
		);
	}
}
