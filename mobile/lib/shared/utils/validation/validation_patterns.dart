class ValidationPatterns {
  static const String EMAIL = r"^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
  static const String IBAN = r"^[A-Z]{2}\d{2}[A-Z0-9]{1,30}$";
  static const String PAYMENT_LINK = r"^(https?://).+$";

  ValidationPatterns._();
}
