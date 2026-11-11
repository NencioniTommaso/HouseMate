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
import 'services/update_service.dart';
import 'core/utils/reload_helper.dart';

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

        home: AppUpdateWrapper(
          child: Consumer<AuthProvider>(
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
      ),
    );
  }
}

/// Root-level widget that listens to Web updates and displays a global update banner.
class AppUpdateWrapper extends StatefulWidget {
  final Widget child;

  const AppUpdateWrapper({super.key, required this.child});

  @override
  State<AppUpdateWrapper> createState() => _AppUpdateWrapperState();
}

class _AppUpdateWrapperState extends State<AppUpdateWrapper>
    with WidgetsBindingObserver {
  final _updateService = UpdateService();

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _updateService.startChecking();
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    _updateService.stop();
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) {
      _updateService.checkForUpdate();
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Column(
        children: [
          ValueListenableBuilder<bool>(
            valueListenable: _updateService.updateAvailable,
            builder: (context, hasUpdate, _) {
              if (!hasUpdate) return const SizedBox.shrink();

              return MaterialBanner(
                backgroundColor: Theme.of(context).colorScheme.primaryContainer,
                leading: const Icon(Icons.system_update_alt),
                content: const Text(
                  'A new version of HouseMate is available.',
                  style: TextStyle(fontWeight: FontWeight.bold),
                ),
                actions: [
                  TextButton(
                    onPressed: reloadApp,
                    child: const Text('Restart to Update'),
                  ),
                ],
              );
            },
          ),
          Expanded(child: widget.child),
        ],
      ),
    );
  }
}
