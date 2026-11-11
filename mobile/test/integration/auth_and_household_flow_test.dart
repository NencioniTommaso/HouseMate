import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/core/constants/app_strings.dart';
import 'package:mobile/ui/screens/auth_screen.dart';
import 'package:mobile/ui/screens/main_screen.dart';
import 'package:mobile/ui/widgets/shared/app_button.dart';

import 'integration_test_helpers.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  group('Auth and Household Integration Flows', () {
    testWidgets('unauthenticated launch renders AuthScreen, login flow transitions to MainScreen',
        (tester) async {
      tester.view.physicalSize = const Size(1200, 1600);
      tester.view.devicePixelRatio = 1.0;
      addTearDown(tester.view.resetPhysicalSize);

      final ctx = createIntegrationTestContext(
        authenticated: false,
        withHousehold: true,
      );

      await tester.pumpWidget(createIntegrationAppHarness(context: ctx));
      await tester.pumpAndSettle();

      // Verify unauthenticated launch lands on AuthScreen
      expect(find.byType(AuthScreen), findsOneWidget);
      expect(find.text('Welcome Back'), findsOneWidget);
      expect(find.widgetWithText(ElevatedButton, 'Login'), findsOneWidget);

      // Enter email and password
      final emailFields = find.widgetWithText(TextField, 'Email');
      final passwordFields = find.widgetWithText(TextField, 'Password');

      await tester.enterText(emailFields, 'alice@example.com');
      await tester.enterText(passwordFields, 'password123');
      await tester.pump();

      // Submit login
      await tester.tap(find.widgetWithText(ElevatedButton, 'Login'));
      await tester.pumpAndSettle();

      // Verify transition to MainScreen
      expect(find.byType(MainScreen), findsOneWidget);
      expect(find.text(AppStrings.yourHousehold), findsOneWidget);
      expect(find.text('The Cool Apartment'), findsOneWidget);
    });

    testWidgets('registration flow creates account and transitions to MainScreen',
        (tester) async {
      tester.view.physicalSize = const Size(1200, 1600);
      tester.view.devicePixelRatio = 1.0;
      addTearDown(tester.view.resetPhysicalSize);

      final ctx = createIntegrationTestContext(
        authenticated: false,
        withHousehold: true,
      );

      await tester.pumpWidget(createIntegrationAppHarness(context: ctx));
      await tester.pumpAndSettle();

      // Toggle to Register mode
      await tester.tap(find.text("Don't have an account? Register"));
      await tester.pumpAndSettle();

      expect(find.text('Create Account'), findsOneWidget);
      expect(find.widgetWithText(ElevatedButton, 'Register'), findsOneWidget);

      // Fill registration form
      await tester.enterText(find.widgetWithText(TextField, 'First Name'), 'Alice');
      await tester.enterText(find.widgetWithText(TextField, 'Last Name'), 'Smith');
      await tester.enterText(find.widgetWithText(TextField, 'Email'), 'alice@example.com');
      await tester.enterText(find.widgetWithText(TextField, 'Password'), 'secret123');
      await tester.enterText(find.widgetWithText(TextField, 'Confirm Password'), 'secret123');
      await tester.pump();

      // Tap Register
      await tester.tap(find.widgetWithText(ElevatedButton, 'Register'));
      await tester.pumpAndSettle();

      // Verify navigation to MainScreen
      expect(find.byType(MainScreen), findsOneWidget);
      expect(find.text('The Cool Apartment'), findsOneWidget);
    });

    testWidgets('user without household can create a household and see active household view',
        (tester) async {
      tester.view.physicalSize = const Size(1200, 1600);
      tester.view.devicePixelRatio = 1.0;
      addTearDown(tester.view.resetPhysicalSize);

      final ctx = createIntegrationTestContext(
        authenticated: true,
        withHousehold: false,
      );

      await tester.pumpWidget(createIntegrationAppHarness(context: ctx));
      await tester.pumpAndSettle();

      // Verify MainScreen rendered with no household state
      expect(find.byType(MainScreen), findsOneWidget);
      expect(find.text("You don't belong to a household yet."), findsOneWidget);
      expect(find.widgetWithText(AppButton, 'Create a Household'), findsOneWidget);

      // Tap Create a Household
      await tester.tap(find.widgetWithText(AppButton, 'Create a Household'));
      await tester.pumpAndSettle();

      expect(find.text('Create a Household'), findsNWidgets(2)); // Button + Sheet title
      expect(find.widgetWithText(ElevatedButton, 'Create'), findsOneWidget);

      // Enter name and create
      await tester.enterText(find.widgetWithText(TextField, 'Household Name'), 'Dream Villa');
      await tester.pump();

      await tester.tap(find.widgetWithText(ElevatedButton, 'Create'));
      await tester.pumpAndSettle();

      // Verify active household view is rendered with the new household
      expect(find.text(AppStrings.yourHousehold), findsOneWidget);
      expect(find.text('Dream Villa'), findsOneWidget);
      expect(find.widgetWithText(AppButton, AppStrings.manageMembers), findsOneWidget);
    });

    testWidgets('user without household can join a household with invite code',
        (tester) async {
      tester.view.physicalSize = const Size(1200, 1600);
      tester.view.devicePixelRatio = 1.0;
      addTearDown(tester.view.resetPhysicalSize);

      final ctx = createIntegrationTestContext(
        authenticated: true,
        withHousehold: false,
      );

      await tester.pumpWidget(createIntegrationAppHarness(context: ctx));
      await tester.pumpAndSettle();

      expect(find.byType(MainScreen), findsOneWidget);
      expect(find.text("You don't belong to a household yet."), findsOneWidget);
      expect(find.widgetWithText(AppButton, 'Join an Existing Household'), findsOneWidget);

      // Tap Join an Existing Household
      await tester.tap(find.widgetWithText(AppButton, 'Join an Existing Household'));
      await tester.pumpAndSettle();

      expect(find.text('Join a Household'), findsOneWidget);
      expect(find.widgetWithText(ElevatedButton, 'Join'), findsOneWidget);

      // Enter invite code and join
      await tester.enterText(find.widgetWithText(TextField, 'Invitation Code'), 'INV-999');
      await tester.pump();

      await tester.tap(find.widgetWithText(ElevatedButton, 'Join'));
      await tester.pumpAndSettle();

      // Verify active household view is rendered with joined household
      expect(find.text(AppStrings.yourHousehold), findsOneWidget);
      expect(find.text('Joined Villa'), findsOneWidget);
      expect(find.widgetWithText(AppButton, AppStrings.manageMembers), findsOneWidget);
    });
  });
}
