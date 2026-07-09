class ApiConfig {
  // Dart automatically sets this to TRUE when you build the app for the App Store
  // and FALSE when you are just pressing "Play" in Android Studio.
  static const bool isProduction = bool.fromEnvironment('dart.vm.product');

  // Automatically swaps between your local PC and your Render cloud server!
  static const String baseUrl = isProduction
      ? 'https://housemate-backend-urlu.onrender.com/api' // Your Prod URL
      : 'http://192.168.1.15:8080/api';                   // Your Local URL
}