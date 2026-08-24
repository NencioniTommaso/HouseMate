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

import 'core/utils/ui_service.dart';

void main() {
  // Ensure the engine is fully booted before we run the app
  WidgetsFlutterBinding.ensureInitialized();

  // Instantiate the single ApiClient and UiService for Dependency Injection
  final apiClient = ApiClient();
  final uiService = UiService();

  runApp(HouseMateApp(apiClient: apiClient, uiService: uiService));
}

class HouseMateApp extends StatelessWidget {
  final ApiClient apiClient;
  final UiService uiService;

  const HouseMateApp({
    super.key, 
    required this.apiClient, 
    required this.uiService,
  });

  @override
  Widget build(BuildContext context) {
    // MultiProvider injects your state globally, just like a Spring Bean!
    return MultiProvider(
      providers: [
        Provider<UiService>.value(value: uiService),
        ChangeNotifierProvider(create: (_) => AuthProvider(apiClient: apiClient, uiService: uiService)),
        ChangeNotifierProvider(create: (_) => UserProvider(apiClient: apiClient, uiService: uiService)),
        ChangeNotifierProvider(create: (_) => HouseholdProvider(apiClient: apiClient, uiService: uiService)),
        ChangeNotifierProvider(create: (_) => ExpenseProvider(apiClient: apiClient, uiService: uiService)),
        ChangeNotifierProvider(create: (_) => ChoreProvider(apiClient: apiClient, uiService: uiService)),
      ],

      child: MaterialApp(
        title: AppStrings.appName,
        theme: AppTheme.light,
        debugShowCheckedModeBanner: false,
        navigatorKey: uiService.navigatorKey,
        scaffoldMessengerKey: uiService.messengerKey,

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