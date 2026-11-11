package com.housemate.shared.utils.validation;

public final class ValidationPatterns {

    public static final String EMAIL = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    public static final String IBAN = "^[A-Z]{2}\\d{2}[A-Z0-9]{1,30}$";
    public static final String PAYMENT_LINK = "^(https?://).+$";

    private ValidationPatterns() {
    }
}