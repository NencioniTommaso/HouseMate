import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'state/auth_provider.dart';
import 'state/user_provider.dart';
import 'state/household_provider.dart';
import 'state/expense_provider.dart';
import 'state/chore_provider.dart';
import 'ui/screens/auth_screen.dart';
import 'ui/screens/main_screen.dart';
import 'core/network/api_client.dart';
import 'core/theme/app_theme.dart';
import 'core/constants/app_strings.dart';

void main() {
  // Ensure the engine is fully booted before we run the app
  WidgetsFlutterBinding.ensureInitialized();

  // Instantiate the single ApiClient for Dependency Injection
  final apiClient = ApiClient();

  runApp(HouseMateApp(apiClient: apiClient));
}

class HouseMateApp extends StatelessWidget {
  final ApiClient apiClient;

  const HouseMateApp({super.key, required this.apiClient});

  @override
  Widget build(BuildContext context) {
    // MultiProvider injects your state globally, just like a Spring Bean!
    return MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => AuthProvider(apiClient: apiClient)),
        ChangeNotifierProvider(create: (_) => UserProvider(apiClient: apiClient)),
        ChangeNotifierProvider(create: (_) => HouseholdProvider(apiClient: apiClient)),
        ChangeNotifierProvider(create: (_) => ExpenseProvider(apiClient: apiClient)),
        ChangeNotifierProvider(create: (_) => ChoreProvider(apiClient: apiClient)),
      ],

      child: MaterialApp(
        title: AppStrings.appName,
        theme: AppTheme.light,
        debugShowCheckedModeBanner: false,

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