package com.housemate.shared.dto.household.request.validation;

import com.housemate.shared.dto.household.request.AddMemberRequestDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class AddMemberRequestDTOValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDownValidator() {
        validatorFactory.close();
    }

    @Test
    void addMemberRequest_validInvitationCode_hasNoViolations() {
        AddMemberRequestDTO dto = new AddMemberRequestDTO("invitation-code-123");

        Set<ConstraintViolation<AddMemberRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void addMemberRequest_maxLengthInvitationCode_hasNoViolations() {
        AddMemberRequestDTO dto = new AddMemberRequestDTO("A".repeat(64));

        Set<ConstraintViolation<AddMemberRequestDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void addMemberRequest_blankInvitationCode_hasNotBlankViolation() {
        AddMemberRequestDTO dto = new AddMemberRequestDTO("");

        Set<ConstraintViolation<AddMemberRequestDTO>> violations = validator.validate(dto);

        assertThat(violations)
            .extracting(
                v -> v.getPropertyPath().toString(),
                v -> v.getConstraintDescriptor().getAnnotation().annotationType(),
                ConstraintViolation::getInvalidValue
            )
            .containsExactly(tuple("invitationCode", NotBlank.class, ""));
    }

    @Test
    void addMemberRequest_nullInvitationCode_hasNotBlankViolation() {
        AddMemberRequestDTO dto = new AddMemberRequestDTO(null);

        Set<ConstraintViolation<AddMemberRequestDTO>> violations = validator.validate(dto);

        assertThat(violations)
            .extracting(
                v -> v.getPropertyPath().toString(),
                v -> v.getConstraintDescriptor().getAnnotation().annotationType(),
                ConstraintViolation::getInvalidValue
            )
            .containsExactly(tuple("invitationCode", NotBlank.class, null));
    }

    @Test
    void addMemberRequest_tooLongInvitationCode_hasSizeViolation() {
        AddMemberRequestDTO dto = new AddMemberRequestDTO("A".repeat(65));

        Set<ConstraintViolation<AddMemberRequestDTO>> violations = validator.validate(dto);

        assertThat(violations)
            .extracting(
                v -> v.getPropertyPath().toString(),
                v -> v.getConstraintDescriptor().getAnnotation().annotationType(),
                ConstraintViolation::getInvalidValue
            )
            .containsExactly(tuple("invitationCode", Size.class, "A".repeat(65)));
    }
}
