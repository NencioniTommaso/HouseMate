class ApiConfig {
  // Dart automatically sets this to TRUE when you build the app for the App Store
  // and FALSE when you are just pressing "Play" in Android Studio.
  static const bool isProduction = bool.fromEnvironment('dart.vm.product');

  static const String baseUrl = "https://api.housemateapp.stream/api";
}