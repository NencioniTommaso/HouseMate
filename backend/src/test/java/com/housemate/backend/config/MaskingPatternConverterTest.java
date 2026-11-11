package com.housemate.backend.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MaskingPatternConverter Unit Tests")
class MaskingPatternConverterTest {

    @Test
    @DisplayName("Masks passwords in key-value and JSON formats")
    void masksPasswords() {
        assertThat(MaskingPatternConverter.mask("password=secret123"))
                .isEqualTo("password=***");
        assertThat(MaskingPatternConverter.mask("password: secret123"))
                .isEqualTo("password: ***");
        assertThat(MaskingPatternConverter.mask("{\"password\": \"secret123\", \"email\": \"user@example.com\"}"))
                .isEqualTo("{\"password\": \"***\", \"email\": \"user@example.com\"}");
    }

    @Test
    @DisplayName("Masks tokens and Bearer authorizations")
    void masksTokens() {
        assertThat(MaskingPatternConverter.mask("token=eyJhbGciOiJIUzI1NiJ9.abc.def"))
                .isEqualTo("token=***");
        assertThat(MaskingPatternConverter.mask("Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.abc.def"))
                .isEqualTo("Authorization: Bearer ***");
        assertThat(MaskingPatternConverter.mask("token: secret-token-val"))
                .isEqualTo("token: ***");
    }

    @Test
    @DisplayName("Masks IBANs in key-value and JSON formats")
    void masksIbans() {
        assertThat(MaskingPatternConverter.mask("iban=IT60X0542811101000000123456"))
                .isEqualTo("iban=***");
        assertThat(MaskingPatternConverter.mask("{\"iban\": \"DE89370400440532013000\"}"))
                .isEqualTo("{\"iban\": \"***\"}");
    }

    @Test
    @DisplayName("Masks invite codes in various hyphen/underscore formats")
    void masksInviteCodes() {
        assertThat(MaskingPatternConverter.mask("invite-code=ABCDEF123"))
                .isEqualTo("invite-code=***");
        assertThat(MaskingPatternConverter.mask("invite_code=ABCDEF123"))
                .isEqualTo("invite_code=***");
        assertThat(MaskingPatternConverter.mask("inviteCode: ABCDEF123"))
                .isEqualTo("inviteCode: ***");
    }

    @Test
    @DisplayName("Does not modify safe logs")
    void leavesSafeLogsUnchanged() {
        String safe = "HTTP GET /api/users/me -> 200 (15ms)";
        assertThat(MaskingPatternConverter.mask(safe)).isEqualTo(safe);
    }
}
