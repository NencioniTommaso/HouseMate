package com.housemate.backend.service.utils;

import java.util.Collections;
import java.util.Objects;

import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

public class UserDetailsTestUtils {
    @NonNull
    public static UserDetails userDetailsOf(@NonNull String username) throws IllegalArgumentException {
        Assert.notNull(username, "username must not be null");
        return Objects.requireNonNull(
            User.builder()
                .username(username)
                .password("")
                .authorities(Collections.emptyList())
                .build(),
            "Unexpectedly created a null UserDetails"
        );
    }
}
