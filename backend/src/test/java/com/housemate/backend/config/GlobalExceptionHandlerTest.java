package com.housemate.backend.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("GlobalExceptionHandler Unit Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    // ============ Tests for handleBadCredentials ============

    @Test
    @DisplayName("handleBadCredentials - should return 401 UNAUTHORIZED with 'Invalid credentials' message")
    void handleBadCredentials_returnsUnauthorizedWithMessage() {
        BadCredentialsException exception = new BadCredentialsException("Invalid login attempt");

        ResponseEntity<String> response = globalExceptionHandler.handleBadCredentials(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isEqualTo("Invalid credentials");
    }

    @Test
    @DisplayName("handleBadCredentials - should always return 'Invalid credentials' regardless of exception message")
    void handleBadCredentials_alwaysReturnsGenericMessage() {
        BadCredentialsException exception = new BadCredentialsException("Some sensitive error details");

        ResponseEntity<String> response = globalExceptionHandler.handleBadCredentials(exception);

        assertThat(response.getBody()).isEqualTo("Invalid credentials");
    }

    // ============ Tests for handleIllegalArgument ============

    @Test
    @DisplayName("handleIllegalArgument - should return 400 BAD_REQUEST with exception message")
    void handleIllegalArgument_returnsBadRequestWithMessage() {
        String errorMessage = "Invalid input parameter";
        IllegalArgumentException exception = new IllegalArgumentException(errorMessage);

        ResponseEntity<String> response = globalExceptionHandler.handleIllegalArgument(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(errorMessage);
    }

    @Test
    @DisplayName("handleIllegalArgument - should return empty message when exception has no message")
    void handleIllegalArgument_withNullMessage_returnsNull() {
        IllegalArgumentException exception = new IllegalArgumentException();

        ResponseEntity<String> response = globalExceptionHandler.handleIllegalArgument(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNull();
    }

    @Test
    @DisplayName("handleIllegalArgument - should preserve the original exception message")
    void handleIllegalArgument_preservesOriginalMessage() {
        String detailedMessage = "The UUID format is invalid: expected format is xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx";
        IllegalArgumentException exception = new IllegalArgumentException(detailedMessage);

        ResponseEntity<String> response = globalExceptionHandler.handleIllegalArgument(exception);

        assertThat(response.getBody()).isEqualTo(detailedMessage);
    }

    // ============ Tests for handleIllegalState ============

    @Test
    @DisplayName("handleIllegalState - should return 500 INTERNAL_SERVER_ERROR with exception message")
    void handleIllegalState_returnsInternalServerErrorWithMessage() {
        String errorMessage = "User must be in an active household to view debts.";
        IllegalStateException exception = new IllegalStateException(errorMessage);

        ResponseEntity<String> response = globalExceptionHandler.handleIllegalState(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo(errorMessage);
    }

    @Test
    @DisplayName("handleIllegalState - should return null body when exception has no message")
    void handleIllegalState_withNullMessage_returnsNull() {
        IllegalStateException exception = new IllegalStateException();

        ResponseEntity<String> response = globalExceptionHandler.handleIllegalState(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();
    }

    // ============ Tests for handleAccessDenied ============

    @Test
    @DisplayName("handleAccessDenied - should return 403 FORBIDDEN with exception message")
    void handleAccessDenied_returnsForbiddenWithMessage() {
        String errorMessage = "Access denied to this resource";
        AccessDeniedException exception = new AccessDeniedException(errorMessage);

        ResponseEntity<String> response = globalExceptionHandler.handleAccessDenied(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isEqualTo(errorMessage);
    }

    @Test
    @DisplayName("handleAccessDenied - should return empty message when exception has no message")
    void handleAccessDenied_withNullMessage_returnsNull() {
        AccessDeniedException exception = new AccessDeniedException("");

        ResponseEntity<String> response = globalExceptionHandler.handleAccessDenied(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    @DisplayName("handleAccessDenied - should preserve the original exception message")
    void handleAccessDenied_preservesOriginalMessage() {
        String detailedMessage = "User does not have permission to access this resource";
        AccessDeniedException exception = new AccessDeniedException(detailedMessage);

        ResponseEntity<String> response = globalExceptionHandler.handleAccessDenied(exception);

        assertThat(response.getBody()).isEqualTo(detailedMessage);
    }

    // ============ Tests for handleValidationExceptions - MethodArgumentNotValidException ============

    @Test
    @DisplayName("handleValidationExceptions - MethodArgumentNotValidException with single field error")
    void handleValidationExceptions_methodArgumentNotValid_singleError() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("objectName", "email", "Invalid email format");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(Collections.singletonList(fieldError));

        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleValidationExceptions(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .containsEntry("error", "Validation Failed")
                .containsEntry("email", "Invalid email format");
    }

    @Test
    @DisplayName("handleValidationExceptions - MethodArgumentNotValidException with multiple field errors")
    void handleValidationExceptions_methodArgumentNotValid_multipleErrors() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        List<ObjectError> errors = new ArrayList<>();
        errors.add(new FieldError("objectName", "email", "Invalid email format"));
        errors.add(new FieldError("objectName", "password", "Password must be at least 8 characters"));
        errors.add(new FieldError("objectName", "name", "Name cannot be blank"));

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(errors);

        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleValidationExceptions(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .containsEntry("error", "Validation Failed")
                .containsEntry("email", "Invalid email format")
                .containsEntry("password", "Password must be at least 8 characters")
                .containsEntry("name", "Name cannot be blank");
    }

    @Test
    @DisplayName("handleValidationExceptions - MethodArgumentNotValidException with null error message defaults to 'Invalid value'")
    void handleValidationExceptions_methodArgumentNotValid_nullErrorMessage() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("objectName", "field", null);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(Collections.singletonList(fieldError));

        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleValidationExceptions(exception);

        assertThat(response.getBody())
                .containsEntry("error", "Validation Failed")
                .containsEntry("field", "Invalid value");
    }

    @Test
    @DisplayName("handleValidationExceptions - MethodArgumentNotValidException with no errors")
    void handleValidationExceptions_methodArgumentNotValid_noErrors() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(Collections.emptyList());

        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleValidationExceptions(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .containsEntry("error", "Validation Failed")
                .hasSize(1);
    }

    // ============ Tests for handleValidationExceptions - BindException ============

    @Test
    @DisplayName("handleValidationExceptions - BindException with single field error")
    void handleValidationExceptions_bindException_singleError() {
        BindException exception = mock(BindException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("objectName", "username", "Username already exists");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(Collections.singletonList(fieldError));

        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleValidationExceptions(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .containsEntry("error", "Validation Failed")
                .containsEntry("username", "Username already exists");
    }

    @Test
    @DisplayName("handleValidationExceptions - BindException with multiple field errors")
    void handleValidationExceptions_bindException_multipleErrors() {
        BindException exception = mock(BindException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        List<ObjectError> errors = new ArrayList<>();
        errors.add(new FieldError("objectName", "firstName", "First name cannot be blank"));
        errors.add(new FieldError("objectName", "lastName", "Last name cannot be blank"));

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(errors);

        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleValidationExceptions(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .containsEntry("error", "Validation Failed")
                .containsEntry("firstName", "First name cannot be blank")
                .containsEntry("lastName", "Last name cannot be blank");
    }

    @Test
    @DisplayName("handleValidationExceptions - BindException with null error message defaults to 'Invalid value'")
    void handleValidationExceptions_bindException_nullErrorMessage() {
        BindException exception = mock(BindException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("objectName", "age", null);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(Collections.singletonList(fieldError));

        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleValidationExceptions(exception);

        assertThat(response.getBody())
                .containsEntry("error", "Validation Failed")
                .containsEntry("age", "Invalid value");
    }

    @Test
    @DisplayName("handleValidationExceptions - BindException with empty error message defaults to 'Invalid value'")
    void handleValidationExceptions_bindException_emptyErrorMessage() {
        BindException exception = mock(BindException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("objectName", "status", "");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(Collections.singletonList(fieldError));

        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleValidationExceptions(exception);

        // Empty string is treated as a valid error message
        assertThat(response.getBody())
                .containsEntry("error", "Validation Failed")
                .containsEntry("status", "");
    }
}


