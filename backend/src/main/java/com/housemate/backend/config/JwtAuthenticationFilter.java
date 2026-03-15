package com.housemate.backend.config;

import com.housemate.backend.service.JwtService;
import com.housemate.backend.service.UserService;

import io.jsonwebtoken.lang.Assert;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;


@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        if (jwt == null) {
            log.error("Unexpectedly null JWT token after removing 'Bearer ' prefix");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String userIdString = jwtService.extractSubject(jwt);

            if (userIdString != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                final UUID userId = parseUuidFailNull(userIdString);
                if (userId == null) {
                    log.warn("Invalid JWT subject format: {}", userIdString);
                    filterChain.doFilter(request, response);
                    return;
                }

                UserDetails userDetails;
                try {
                    userDetails = userService.loadUserById(userId);
                } catch (UsernameNotFoundException e) {
                    log.warn("Failed to load user by ID from JWT subject: {}", e.getMessage());
                    filterChain.doFilter(request, response);
                    return;
                }

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            log.warn("JWT processing failed: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private UUID parseUuidFailNull(@NonNull String uuidString) {
        Assert.notNull(uuidString, "uuidString must not be null");
        try {
            return UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
