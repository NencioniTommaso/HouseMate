package com.housemate.backend.service.utils;

import com.housemate.shared.utils.validation.ValidationPatterns;

import java.net.URI;
import java.util.regex.Pattern;

public final class UserValidationUtils {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(ValidationPatterns.EMAIL);
    private static final Pattern IBAN_PATTERN = Pattern.compile(ValidationPatterns.IBAN);
    private static final Pattern PAYMENT_LINK_PATTERN = Pattern.compile(ValidationPatterns.PAYMENT_LINK);

    private UserValidationUtils() {
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidIban(String iban) {
        return iban != null && IBAN_PATTERN.matcher(iban).matches();
    }

    public static boolean isValidPaymentLink(String paymentLink) {
        if (paymentLink == null || !PAYMENT_LINK_PATTERN.matcher(paymentLink).matches()) {
            return false;
        }

        try {
            URI parsedUri = URI.create(paymentLink);
            return parsedUri.getHost() != null && !parsedUri.getHost().isBlank();
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}