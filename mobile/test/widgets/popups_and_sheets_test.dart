import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/core/utils/ui_service.dart';
import 'package:mobile/shared/dto/expense/response/debt_response_dto.dart';
import 'package:mobile/shared/dto/household/response/household_invitation_code_response_dto.dart';
import 'package:mobile/shared/dto/household/response/household_member_response_dto.dart';
import 'package:mobile/shared/dto/household/response/household_membership_response_dto.dart';
import 'package:mobile/shared/dto/household/response/household_response_dto.dart';
import 'package:mobile/shared/dto/user/response/user_response_dto.dart';
import 'package:mobile/shared/enums/user_transaction_role.dart';
import 'package:mobile/state/auth_provider.dart';
import 'package:mobile/state/chore_provider.dart';
import 'package:mobile/state/expense_provider.dart';
import 'package:mobile/state/household_provider.dart';
import 'package:mobile/ui/popups/dialog_confirm_action.dart';
import 'package:mobile/ui/popups/expenses/sheet_create_expense.dart';
import 'package:mobile/ui/popups/expenses/sheet_settle_debt.dart';
import 'package:mobile/ui/popups/household/dialog_invite_member.dart';
import 'package:mobile/ui/popups/household/sheet_create_chore.dart';
import 'package:mobile/ui/popups/household/sheet_join_household.dart';
import 'package:mobile/ui/popups/no-household/sheet_create_household.dart';
import 'package:provider/provider.dart';

import '../services/test_helpers.dart';
import '../state/state_test_helpers.dart';

Widget wrapWithProviders({
  required Widget child,
  HouseholdProvider? householdProvider,
  AuthProvider? authProvider,
  ExpenseProvider? expenseProvider,
  ChoreProvider? choreProvider,
  FakeUiService? uiService,
}) {
  final testContext = createTestContext();
  final ui = uiService ?? FakeUiService();

  return MultiProvider(
    providers: [
      Provider<UiService>.value(value: ui),
      ChangeNotifierProvider<HouseholdProvider>.value(
        value: householdProvider ??
            HouseholdProvider(
              apiClient: testContext.apiClient,
              uiService: ui,
            ),
      ),
      ChangeNotifierProvider<AuthProvider>.value(
        value: authProvider ??
            AuthProvider(
              apiClient: testContext.apiClient,
              storage: testContext.secureStorage,
              uiService: ui,
            ),
      ),
      ChangeNotifierProvider<ExpenseProvider>.value(
        value: expenseProvider ??
            ExpenseProvider(
              apiClient: testContext.apiClient,
              uiService: ui,
            ),
      ),
      ChangeNotifierProvider<ChoreProvider>.value(
        value: choreProvider ??
            ChoreProvider(
              apiClient: testContext.apiClient,
              uiService: ui,
            ),
      ),
    ],
    child: MaterialApp(
      home: Scaffold(
        body: child,
      ),
    ),
  );
}

void main() {
  group('DialogConfirmAction', () {
    testWidgets('renders title and message, cancels without callback', (tester) async {
      var confirmed = false;

      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: Builder(
              builder: (context) => ElevatedButton(
                onPressed: () => showConfirmActionDialog(
                  context: context,
                  title: 'Delete Item',
                  message: 'Are you sure?',
                  onConfirm: () => confirmed = true,
                ),
                child: const Text('Open Dialog'),
              ),
            ),
          ),
        ),
      );

      await tester.tap(find.text('Open Dialog'));
      await tester.pumpAndSettle();

      expect(find.text('Delete Item'), findsOneWidget);
      expect(find.text('Are you sure?'), findsOneWidget);

      await tester.tap(find.text('Cancel'));
      await tester.pumpAndSettle();

      expect(confirmed, isFalse);
      expect(find.text('Delete Item'), findsNothing);
    });

    testWidgets('confirms and triggers onConfirm callback', (tester) async {
      var confirmed = false;

      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: Builder(
              builder: (context) => ElevatedButton(
                onPressed: () => showConfirmActionDialog(
                  context: context,
                  title: 'Confirm Operation',
                  message: 'Proceed?',
                  onConfirm: () => confirmed = true,
                ),
                child: const Text('Open Dialog'),
              ),
            ),
          ),
        ),
      );

      await tester.tap(find.text('Open Dialog'));
      await tester.pumpAndSettle();

      await tester.tap(find.text('Confirm'));
      await tester.pumpAndSettle();

      expect(confirmed, isTrue);
      expect(find.text('Confirm Operation'), findsNothing);
    });
  });

  group('DialogInviteMember', () {
    testWidgets('renders invitation dialog and code', (tester) async {
      final testContext = createTestContext();
      final uiService = FakeUiService();
      final householdProvider = HouseholdProvider(
        apiClient: testContext.apiClient,
        uiService: uiService,
      );

      await tester.pumpWidget(
        wrapWithProviders(
          householdProvider: householdProvider,
          child: Builder(
            builder: (context) => ElevatedButton(
              onPressed: () => showInviteMemberDialog(context),
              child: const Text('Invite'),
            ),
          ),
        ),
      );

      await tester.tap(find.text('Invite'));
      await tester.pumpAndSettle();

      expect(find.text('Invite a Member'), findsOneWidget);
    });
  });

  group('SheetCreateHousehold', () {
    testWidgets('renders create household sheet with textfield and button', (tester) async {
      await tester.pumpWidget(
        wrapWithProviders(
          child: Builder(
            builder: (context) => ElevatedButton(
              onPressed: () => showCreateHouseholdSheet(context),
              child: const Text('Create'),
            ),
          ),
        ),
      );

      await tester.tap(find.text('Create'));
      await tester.pumpAndSettle();

      expect(find.text('Create a Household'), findsOneWidget);
      expect(find.byType(TextField), findsOneWidget);
    });
  });

  group('SheetJoinHousehold', () {
    testWidgets('renders join household sheet with code input', (tester) async {
      await tester.pumpWidget(
        wrapWithProviders(
          child: Builder(
            builder: (context) => ElevatedButton(
              onPressed: () => showJoinHouseholdSheet(context),
              child: const Text('Join'),
            ),
          ),
        ),
      );

      await tester.tap(find.text('Join'));
      await tester.pumpAndSettle();

      expect(find.text('Join a Household'), findsOneWidget);
      expect(find.byType(TextField), findsOneWidget);
    });
  });

  group('SheetCreateChore', () {
    testWidgets('renders create chore sheet and validates empty description', (tester) async {
      final uiService = FakeUiService();
      final testContext = createTestContext();
      final householdProvider = HouseholdProvider(
        apiClient: testContext.apiClient,
        uiService: uiService,
      );
      householdProvider.currentHouseholdForTesting = HouseholdResponseDTO(
        id: 'h-1',
        name: 'Test House',
        creationDate: DateTime.now(),
        memberships: [],
      );

      await tester.pumpWidget(
        wrapWithProviders(
          householdProvider: householdProvider,
          uiService: uiService,
          child: Builder(
            builder: (context) => ElevatedButton(
              onPressed: () => showCreateChoreSheet(context),
              child: const Text('New Chore'),
            ),
          ),
        ),
      );

      await tester.tap(find.text('New Chore'));
      await tester.pumpAndSettle();

      expect(find.text('New Chore'), findsNWidgets(2)); // Button clicked + Modal title
      expect(find.widgetWithText(ElevatedButton, 'Create'), findsOneWidget);

      // Tap create without filling name
      await tester.tap(find.widgetWithText(ElevatedButton, 'Create'));
      await tester.pump();

      expect(uiService.errorMessages.contains('Please enter a chore name'), isTrue);
    });
  });

  group('SheetCreateExpense', () {
    testWidgets('renders create expense sheet with inputs', (tester) async {
      await tester.pumpWidget(
        wrapWithProviders(
          child: Builder(
            builder: (context) => ElevatedButton(
              onPressed: () => showCreateExpenseSheet(context),
              child: const Text('New Expense'),
            ),
          ),
        ),
      );

      await tester.tap(find.text('New Expense'));
      await tester.pumpAndSettle();

      expect(find.text('Add Expense'), findsOneWidget);
    });
  });

  group('SheetSettleDebt', () {
    testWidgets('renders settle debt sheet with debt details', (tester) async {
      final debt = DebtResponseDTO(
        debtId: 'd-1',
        userTransactionRole: UserTransactionRole.debtor,
        involvedId: 'u-2',
        involvedName: 'Alice',
        amount: 25.0,
      );

      await tester.pumpWidget(
        wrapWithProviders(
          child: Builder(
            builder: (context) => ElevatedButton(
              onPressed: () => showSettleDebtSheet(context, debt),
              child: const Text('Settle'),
            ),
          ),
        ),
      );

      await tester.tap(find.text('Settle'));
      await tester.pumpAndSettle();

      expect(find.text('Settle Debt'), findsNWidgets(2)); // Header + submit button
      expect(find.text('Amount to pay back:'), findsOneWidget);
      expect(find.text('Exact amount:'), findsOneWidget);
    });
  });
}
