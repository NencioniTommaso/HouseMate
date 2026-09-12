class ValidationPatterns {
  static const String email = r"^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
  static const String iban = r"^[A-Z]{2}\d{2}[A-Z0-9]{1,30}$";
  static const String paymentLink = r"^(https?://).+$";

  ValidationPatterns._();
}
