package com.housemate.client.service;

import com.housemate.client.service.context.AuthState;
import com.housemate.client.service.context.SessionManager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HttpRestClientTest {

    @Mock
    private SessionManager context;

    @InjectMocks
    private HttpRestClient httpRestClient;

    // --- Auth Header Tests ---

    @Test
    void buildAuthHeader_existingJwt_success() {
        AuthState mockAuthState = mock(AuthState.class);
        when(context.getAuthState()).thenReturn(mockAuthState);
        when(mockAuthState.hasJwt()).thenReturn(true);
        when(mockAuthState.getJwt()).thenReturn("valid.jwt.token");

        String result = httpRestClient.buildAuthHeader();

        assertThat(result).isEqualTo("Bearer valid.jwt.token");
    }

    @Test
    void buildAuthHeader_missingJwt_throwsRuntimeException() {
        AuthState mockAuthState = mock(AuthState.class);
        when(context.getAuthState()).thenReturn(mockAuthState);
        when(mockAuthState.hasJwt()).thenReturn(false);

        assertThatThrownBy(() -> httpRestClient.buildAuthHeader())
                .isInstanceOf(RuntimeException.class);

        verify(mockAuthState, never()).getJwt();
    }
}