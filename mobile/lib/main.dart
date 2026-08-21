import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'state/auth_provider.dart';
import 'state/user_provider.dart';
import 'state/household_provider.dart';
import 'state/expense_provider.dart';
import 'state/chore_provider.dart';
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
        ChangeNotifierProvider(create: (_) => UserProvider()),
        ChangeNotifierProvider(create: (_) => HouseholdProvider()),
        ChangeNotifierProvider(create: (_) => ExpenseProvider()),
        ChangeNotifierProvider(create: (_) => ChoreProvider()),
      ],

      child: MaterialApp(
        title: 'HouseMate',

        theme: ThemeData(
          useMaterial3: true,
          colorScheme: ColorScheme.fromSeed(
            seedColor: const Color(0xFF2C3E50),
            primary: const Color(0xFF2C3E50),
            secondary: const Color(0xFF3498DB),
          ),
          scaffoldBackgroundColor: const Color(0xFFF4F6F8),
          appBarTheme: const AppBarTheme(
            backgroundColor: Color(0xFF2C3E50),
            foregroundColor: Colors.white,
            elevation: 0,
          ),
          textTheme: const TextTheme(
            titleLarge: TextStyle(
              fontSize: 24,
              fontWeight: FontWeight.bold,
              color: Color(0xFF2C3E50),
            ),
            bodyMedium: TextStyle(
              fontSize: 13,
              color: Color(0xFF2C3E50),
            ),
          ),
        ),

        home: Consumer<AuthProvider>(
          builder: (context, authState, _) {

            if (authState.isCheckingSession) {
              return const Scaffold(
                body: Center(
                  child: CircularProgressIndicator(),
                ),
              );
            }

            if (authState.isAuthenticated) {
              return const MainScreen();
            } else {
              return const AuthScreen();
            }
          },
        ),
      ),
    );
  }
}