import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'state/auth_provider.dart';
import 'ui/screens/auth_screen.dart';
import 'ui/screens/main_screen.dart';

void main() {
  // Ensure the engine is fully booted before we run the app
  WidgetsFlutterBinding.ensureInitialized();

  runApp(const HouseMateApp());
}

class HouseMateApp extends StatelessWidget {
  const HouseMateApp({super.key});

  @override
  Widget build(BuildContext context) {
    // MultiProvider injects your state globally, just like a Spring Bean!
    return MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => AuthProvider()),
        // You will add HouseholdProvider here later
      ],
      child: MaterialApp(
        title: 'HouseMate',
        theme: ThemeData(
          primarySwatch: Colors.blue,
          useMaterial3: true,
        ),
        // Consumer rebuilds the starting screen if AuthProvider changes
        home: Consumer<AuthProvider>(
          builder: (context, authProvider, _) {
            // This is your "Remember Me" routing logic
            if (authProvider.isAuthenticated) {
              return const MainScreen(); // They have a token, skip login!
            } else {
              return const AuthScreen(); // No token, show login
            }
          },
        ),
      ),
    );
  }
}