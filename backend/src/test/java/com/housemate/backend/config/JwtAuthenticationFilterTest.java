package com.housemate.backend.config;

import com.housemate.backend.service.JwtService;
import com.housemate.backend.service.UserService;
import com.housemate.backend.service.utils.UserDetailsTestUtils;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserService userService;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // --- Requests without a valid Bearer token ---

    @Test
    void noAuthorizationHeader_filterPassesThrough_noAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        jwtAuthenticationFilter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtService, userService);
    }

    @Test
    void nonBearerAuthorizationHeader_filterPassesThrough_noAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        jwtAuthenticationFilter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtService, userService);
    }

    @Test
    void bearerHeaderWithNullSubject_filterPassesThrough_noAuthentication() throws Exception {
        String token = "some.jwt.token";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(jwtService.extractSubject(token)).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(userService);
    }

    @Test
    void bearerHeaderWithInvalidUuidSubject_filterPassesThrough_noAuthentication() throws Exception {
        String token = "some.jwt.token";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(jwtService.extractSubject(token)).thenReturn("not-a-valid-uuid");

        jwtAuthenticationFilter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(userService);
    }

    @Test
    void bearerHeaderWithValidTokenButUserNotFound_noAuthentication() throws Exception {
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        String token = "valid.jwt.token";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(jwtService.extractSubject(token)).thenReturn(userId);
        when(userService.loadUserById(Objects.requireNonNull(UUID.fromString(userId))))
                .thenThrow(new UsernameNotFoundException("User not found"));

        jwtAuthenticationFilter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    // --- Token validation outcomes ---

    @Test
    void validBearerToken_setsAuthenticationInSecurityContext() throws Exception {
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        String token = "valid.jwt.token";
        UserDetails userDetails = UserDetailsTestUtils.userDetailsOf(userId);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(jwtService.extractSubject(token)).thenReturn(userId);
        when(userService.loadUserById(
            Objects.requireNonNull(UUID.fromString(userId))
        )).thenReturn(userDetails);
        when(jwtService.isTokenValid(token, userDetails)).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
                .isEqualTo(userId);
    }

    @Test
    void invalidBearerToken_doesNotSetAuthentication() throws Exception {
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        String token = "invalid.jwt.token";
        UserDetails userDetails = UserDetailsTestUtils.userDetailsOf(userId);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(jwtService.extractSubject(token)).thenReturn(userId);
        when(userService.loadUserById(
            Objects.requireNonNull(UUID.fromString(userId))
        )).thenReturn(userDetails);
        when(jwtService.isTokenValid(token, userDetails)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    // --- JwtException handling (must not propagate as 500) ---

    @Test
    void expiredToken_jwtExceptionCaught_filterPassesThrough_noAuthentication() throws Exception {
        String token = "expired.jwt.token";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(jwtService.extractSubject(token))
                .thenThrow(new ExpiredJwtException(null, null, "Token expired"));

        jwtAuthenticationFilter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(response.getStatus()).isEqualTo(200); // No error response set by the filter itself
    }

    @Test
    void malformedToken_jwtExceptionCaught_filterPassesThrough_noAuthentication() throws Exception {
        String token = "malformed";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(jwtService.extractSubject(token))
                .thenThrow(new MalformedJwtException("Malformed token"));

        jwtAuthenticationFilter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void anyExceptionCaught_filterPassesThrough_noAuthentication() throws Exception {
        String token = "malformed";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(jwtService.extractSubject(token))
                .thenThrow(new NullPointerException("Generic Exception"));

        jwtAuthenticationFilter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    // --- Filter chain always continues ---

    @Test
    void validToken_filterChainContinues() throws Exception {
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        String token = "valid.jwt.token";
        UserDetails userDetails = UserDetailsTestUtils.userDetailsOf(userId);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockFilterChain chain = new MockFilterChain();

        when(jwtService.extractSubject(token)).thenReturn(userId);
        when(userService.loadUserById(
            Objects.requireNonNull(UUID.fromString(userId))
        )).thenReturn(userDetails);
        when(jwtService.isTokenValid(token, userDetails)).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, new MockHttpServletResponse(), chain);

        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void noToken_filterChainContinues() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockFilterChain chain = new MockFilterChain();

        jwtAuthenticationFilter.doFilterInternal(request, new MockHttpServletResponse(), chain);

        assertThat(chain.getRequest()).isNotNull();
    }

    // --- Security context pre-populated ---

    @Test
    void alreadyAuthenticated_doesNotCallUserService() throws Exception {
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        String token = "valid.jwt.token";
        UserDetails userDetails = UserDetailsTestUtils.userDetailsOf(userId);

        // Pre-populate the security context
        UsernamePasswordAuthenticationToken existingAuth = new UsernamePasswordAuthenticationToken(
            userDetails, null, Collections.emptyList()
        );
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(jwtService.extractSubject(token)).thenReturn(userId);

        jwtAuthenticationFilter.doFilterInternal(request, response, chain);

        verifyNoInteractions(userService);
    }
}
