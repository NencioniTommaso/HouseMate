import 'dart:convert';
import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/core/constants/app_strings.dart';
import 'package:mobile/core/utils/ui_service.dart';
import 'package:mobile/shared/dto/household/response/household_response_dto.dart';
import 'package:mobile/state/auth_provider.dart';
import 'package:mobile/state/chore_provider.dart';
import 'package:mobile/state/expense_provider.dart';
import 'package:mobile/state/household_provider.dart';
import 'package:mobile/state/user_provider.dart';
import 'package:mobile/ui/screens/tabs/assignments_screen.dart';
import 'package:mobile/ui/screens/tabs/expenses_screen.dart';
import 'package:mobile/ui/screens/tabs/household_screen.dart';
import 'package:mobile/ui/screens/tabs/user_screen.dart';
import 'package:mobile/ui/widgets/chore_assignment_item_element.dart';
import 'package:mobile/ui/widgets/expense_item_element.dart';
import 'package:mobile/ui/widgets/shared/app_button.dart';
import 'package:mobile/ui/widgets/shared/app_empty_state.dart';
import 'package:mobile/ui/widgets/shared/app_header.dart';
import 'package:provider/provider.dart';

import '../services/test_helpers.dart';
import '../state/state_test_helpers.dart';

Widget buildTabsHarness({
  required Widget child,
  ChoreProvider? choreProvider,
  HouseholdProvider? householdProvider,
  AuthProvider? authProvider,
  ExpenseProvider? expenseProvider,
  UserProvider? userProvider,
  FakeUiService? uiService,
  TestContext? testContext,
}) {
  final ctx = testContext ?? createTestContext();
  final ui = uiService ?? FakeUiService();

  return MultiProvider(
    providers: [
      Provider<UiService>.value(value: ui),
      ChangeNotifierProvider<AuthProvider>.value(
        value: authProvider ??
            AuthProvider(
              apiClient: ctx.apiClient,
              storage: ctx.secureStorage,
              uiService: ui,
            ),
      ),
      ChangeNotifierProvider<HouseholdProvider>.value(
        value: householdProvider ??
            HouseholdProvider(
              apiClient: ctx.apiClient,
              uiService: ui,
            ),
      ),
      ChangeNotifierProvider<ExpenseProvider>.value(
        value: expenseProvider ??
            ExpenseProvider(
              apiClient: ctx.apiClient,
              uiService: ui,
            ),
      ),
      ChangeNotifierProvider<ChoreProvider>.value(
        value: choreProvider ??
            ChoreProvider(
              apiClient: ctx.apiClient,
              uiService: ui,
            ),
      ),
      ChangeNotifierProvider<UserProvider>.value(
        value: userProvider ??
            UserProvider(
              apiClient: ctx.apiClient,
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

void configureMockAdapter(MockHttpClientAdapter adapter, {
  Map<String, dynamic>? userJson,
  Map<String, dynamic>? householdJson,
  List<dynamic>? assignmentsJson,
  Map<String, dynamic>? choreOverviewJson,
  List<dynamic>? choresJson,
  Map<String, dynamic>? expenseOverviewJson,
  Map<String, dynamic>? userNetOverviewJson,
  List<dynamic>? debtsJson,
  List<dynamic>? expensesJson,
  List<dynamic>? settlementsJson,
}) {
  adapter.setHandler((options) {
    final path = options.path;
    dynamic data;

    if (path.contains('/chores/assignments/overview') || path.contains('/chores/assignments/me')) {
      data = choreOverviewJson ?? {'pendingAssignments': 3, 'overdueAssignments': 1};
    } else if (path.contains('/chores/assignments/filter') || path.contains('/chores/assignments')) {
      data = assignmentsJson ?? [];
    } else if (path.contains('/chores')) {
      data = choresJson ?? [];
    } else if (path.contains('/expenses/overview')) {
      data = expenseOverviewJson ?? {'totalAmount': 250.0, 'expenseCount': 5};
    } else if (path.contains('/expenses/me') || path.contains('/expenses/overview/net')) {
      data = userNetOverviewJson ?? {'actualCashFlowAmount': 150.0};
    } else if (path.contains('/debts') || path.contains('/expenses/debts')) {
      data = debtsJson ?? [];
    } else if (path.contains('/settlements')) {
      data = settlementsJson ?? [];
    } else if (path.contains('/expenses')) {
      data = expensesJson ?? [];
    } else if (path.contains('/users/me')) {
      data = userJson ?? {
        'id': 'u-1',
        'name': 'John',
        'surname': 'Doe',
        'email': 'john.doe@example.com',
        'iban': 'DE89370400440532013000',
        'paymentLink': 'https://paypal.me/johndoe',
      };
    } else if (path.contains('/households/current') || path.contains('/households/me')) {
      if (householdJson == null) {
        return ResponseBody.fromString(
          '{"message": "No household found"}',
          404,
          headers: {Headers.contentTypeHeader: [Headers.jsonContentType]},
        );
      }
      data = householdJson;
    } else {
      data = {};
    }

    return ResponseBody.fromString(
      jsonEncode(data),
      200,
      headers: {
        Headers.contentTypeHeader: [Headers.jsonContentType],
      },
    );
  });
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  group('AssignmentsScreen', () {
    testWidgets('renders header, overview, week navigator, and empty state when no chores', (tester) async {
      tester.view.physicalSize = const Size(1200, 1600);
      tester.view.devicePixelRatio = 1.0;
      addTearDown(tester.view.resetPhysicalSize);

      final ctx = createTestContext();
      configureMockAdapter(ctx.adapter, choreOverviewJson: {
        'pendingAssignments': 2,
        'overdueAssignments': 1,
      });

      await tester.pumpWidget(buildTabsHarness(
        testContext: ctx,
        child: const AssignmentsScreen(),
      ));

      await tester.pumpAndSettle();

      // Check header
      expect(find.text(AppStrings.assignmentsTitle), findsOneWidget);
      expect(find.text('Overview: 2 pending, 1 overdue'), findsOneWidget);
      expect(find.widgetWithText(AppButton, AppStrings.newLabel), findsOneWidget);

      // Check week days are rendered
      expect(find.text('Mon'), findsOneWidget);
      expect(find.text('Sun'), findsOneWidget);

      // Check empty state
      expect(find.text(AppStrings.assignmentsNoResults), findsOneWidget);
      expect(find.byIcon(Icons.assignment_turned_in_outlined), findsOneWidget);
    });

    testWidgets('renders assignments list when chores exist for selected day', (tester) async {
      tester.view.physicalSize = const Size(1200, 1600);
      tester.view.devicePixelRatio = 1.0;
      addTearDown(tester.view.resetPhysicalSize);

      final now = DateTime.now();
      final todayAtNoon = DateTime(now.year, now.month, now.day, 12, 0);

      final ctx = createTestContext();
      configureMockAdapter(ctx.adapter, assignmentsJson: [
        {
          'assignmentId': 'assign-1',
          'choreId': 'chore-1',
          'choreDescription': 'Clean Kitchen',
          'assignedUser': {
            'id': 'u-1',
            'name': 'John',
            'surname': 'Doe',
            'email': 'john.doe@example.com',
          },
          'status': 'PENDING',
          'dueDate': todayAtNoon.toIso8601String(),
        },
      ]);

      await tester.pumpWidget(buildTabsHarness(
        testContext: ctx,
        child: const AssignmentsScreen(),
      ));

      await tester.pumpAndSettle();

      expect(find.byType(ChoreAssignmentItemElement), findsOneWidget);
      expect(find.text('Clean Kitchen'), findsOneWidget);
      expect(find.textContaining('Assigned to: John'), findsOneWidget);
    });

    testWidgets('week forward and backward navigation buttons update week', (tester) async {
      tester.view.physicalSize = const Size(1200, 1600);
      tester.view.devicePixelRatio = 1.0;
      addTearDown(tester.view.resetPhysicalSize);

      final ctx = createTestContext();
      configureMockAdapter(ctx.adapter);

      await tester.pumpWidget(buildTabsHarness(
        testContext: ctx,
        child: const AssignmentsScreen(),
      ));

      await tester.pumpAndSettle();

      expect(find.byIcon(Icons.arrow_back), findsOneWidget);
      expect(find.byIcon(Icons.arrow_forward), findsOneWidget);

      // Tap forward
      await tester.tap(find.byIcon(Icons.arrow_forward));
      await tester.pumpAndSettle();

      // Tap backward
      await tester.tap(find.byIcon(Icons.arrow_back));
      await tester.pumpAndSettle();
    });
  });

  group('ExpensesScreen', () {
    testWidgets('renders header, overview, summary cards, and empty state', (tester) async {
      tester.view.physicalSize = const Size(1200, 1600);
      tester.view.devicePixelRatio = 1.0;
      addTearDown(tester.view.resetPhysicalSize);

      final ctx = createTestContext();
      configureMockAdapter(ctx.adapter,
        expenseOverviewJson: {'totalAmount': 120.50, 'expenseCount': 4},
        debtsJson: [
          {
            'debtId': 'd-1',
            'userTransactionRole': 'DEBTOR',
            'involvedId': 'u-2',
            'involvedName': 'Alice',
            'amount': 30.0,
          },
          {
            'debtId': 'd-2',
            'userTransactionRole': 'CREDITOR',
            'involvedId': 'u-3',
            'involvedName': 'Bob',
            'amount': 50.0,
          },
        ],
      );

      await tester.pumpWidget(buildTabsHarness(
        testContext: ctx,
        child: const ExpensesScreen(),
      ));

      await tester.pumpAndSettle();

      // Header
      expect(find.widgetWithText(AppHeader, AppStrings.expensesTitle), findsOneWidget);
      expect(find.textContaining('This Month:'), findsOneWidget);
      expect(find.textContaining('4 expenses'), findsOneWidget);
      expect(find.widgetWithText(AppButton, AppStrings.newLabel), findsOneWidget);

      // Summary Cards
      expect(find.text(AppStrings.youOwe), findsOneWidget);
      expect(find.text(AppStrings.youAreOwed), findsOneWidget);
      expect(find.text('€30.00'), findsOneWidget);
      expect(find.text('€50.00'), findsOneWidget);

      // Filters section & empty state
      expect(find.text('Debtor'), findsOneWidget);
      expect(find.text('Creditor'), findsOneWidget);
      expect(find.text('All'), findsOneWidget);
      expect(find.text('No results found matching your filters.'), findsOneWidget);
    });

    testWidgets('renders expense item elements when expenses list is populated', (tester) async {
      tester.view.physicalSize = const Size(1200, 1600);
      tester.view.devicePixelRatio = 1.0;
      addTearDown(tester.view.resetPhysicalSize);

      final ctx = createTestContext();
      configureMockAdapter(ctx.adapter, expensesJson: [
        {
          'id': 'exp-1',
          'description': 'Groceries',
          'date': DateTime.now().toIso8601String(),
          'amount': 40.0,
          'payerId': 'u-1',
          'payerFullName': 'John Doe',
          'splitType': 'EQUAL_SPLIT',
          'householdId': 'h-1',
          'shares': [
            {
              'id': 'sh-1',
              'userId': 'u-1',
              'userFullName': 'John Doe',
              'amount': 20.0,
            },
            {
              'id': 'sh-2',
              'userId': 'u-2',
              'userFullName': 'Alice Smith',
              'amount': 20.0,
            },
          ],
        },
      ]);

      await tester.pumpWidget(buildTabsHarness(
        testContext: ctx,
        child: const ExpensesScreen(),
      ));

      await tester.pumpAndSettle();

      expect(find.byType(ExpenseItemElement), findsOneWidget);
      expect(find.text('Groceries'), findsOneWidget);
      expect(find.text('Paid by John Doe'), findsOneWidget);
    });
  });

  group('HouseholdScreen', () {
    testWidgets('renders no-household state when user has no household', (tester) async {
      tester.view.physicalSize = const Size(1200, 1600);
      tester.view.devicePixelRatio = 1.0;
      addTearDown(tester.view.resetPhysicalSize);

      final ctx = createTestContext();
      configureMockAdapter(ctx.adapter, householdJson: null);

      await tester.pumpWidget(buildTabsHarness(
        testContext: ctx,
        child: const HouseholdScreen(),
      ));

      await tester.pumpAndSettle();

      expect(find.byIcon(Icons.house_outlined), findsOneWidget);
      expect(find.text("You don't belong to a household yet."), findsOneWidget);
      expect(find.widgetWithText(AppButton, 'Create a Household'), findsOneWidget);
      expect(find.widgetWithText(AppButton, 'Join an Existing Household'), findsOneWidget);
    });

    testWidgets('renders household dashboard with action buttons when user has household', (tester) async {
      tester.view.physicalSize = const Size(1200, 1600);
      tester.view.devicePixelRatio = 1.0;
      addTearDown(tester.view.resetPhysicalSize);

      final ctx = createTestContext();
      configureMockAdapter(ctx.adapter, householdJson: {
        'id': 'h-100',
        'name': 'Pineapple Villa',
        'creationDate': DateTime.now().toIso8601String(),
        'memberships': [],
      });

      await tester.pumpWidget(buildTabsHarness(
        testContext: ctx,
        child: const HouseholdScreen(),
      ));

      await tester.pumpAndSettle();

      expect(find.text(AppStrings.yourHousehold), findsOneWidget);
      expect(find.text('Pineapple Villa'), findsOneWidget);
      expect(find.widgetWithText(AppButton, AppStrings.manageMembers), findsOneWidget);
      expect(find.widgetWithText(AppButton, AppStrings.choresList), findsOneWidget);
      expect(find.widgetWithText(AppButton, AppStrings.shoppingLists), findsOneWidget);
      expect(find.widgetWithText(AppButton, AppStrings.inviteMember), findsOneWidget);
    });
  });

  group('UserScreen', () {
    testWidgets('renders user profile info and logout button', (tester) async {
      tester.view.physicalSize = const Size(1200, 1600);
      tester.view.devicePixelRatio = 1.0;
      addTearDown(tester.view.resetPhysicalSize);

      final ctx = createTestContext();
      configureMockAdapter(ctx.adapter, userJson: {
        'id': 'u-1',
        'name': 'Alice',
        'surname': 'Smith',
        'email': 'alice.smith@example.com',
        'iban': 'FR7630006000011234567890189',
        'paymentLink': 'https://revolut.me/alicesmith',
      });

      await tester.pumpWidget(buildTabsHarness(
        testContext: ctx,
        child: const UserScreen(),
      ));

      await tester.pumpAndSettle();

      expect(find.text(AppStrings.yourProfile), findsOneWidget);
      expect(find.text('Alice'), findsOneWidget);
      expect(find.text('Smith'), findsOneWidget);
      expect(find.text('alice.smith@example.com'), findsOneWidget);
      expect(find.text('FR7630006000011234567890189'), findsOneWidget);
      expect(find.text('https://revolut.me/alicesmith'), findsOneWidget);
      expect(find.widgetWithText(AppButton, AppStrings.edit), findsOneWidget);
      expect(find.widgetWithText(AppButton, AppStrings.logout), findsOneWidget);
    });

    testWidgets('renders performance cards and leave household button when hasHousehold is true', (tester) async {
      tester.view.physicalSize = const Size(1200, 1600);
      tester.view.devicePixelRatio = 1.0;
      addTearDown(tester.view.resetPhysicalSize);

      final ctx = createTestContext();
      configureMockAdapter(ctx.adapter,
        householdJson: {
          'id': 'h-100',
          'name': 'Pineapple Villa',
          'creationDate': DateTime.now().toIso8601String(),
          'memberships': [],
        },
        choreOverviewJson: {
          'pendingAssignments': 5,
          'overdueAssignments': 2,
        },
        userNetOverviewJson: {
          'actualCashFlowAmount': 85.50,
          'projectedCashFlowAmount': 85.50,
        },
      );

      final householdProv = HouseholdProvider(
        apiClient: ctx.apiClient,
        uiService: FakeUiService(),
      );
      householdProv.currentHouseholdForTesting = HouseholdResponseDTO(
        id: 'h-100',
        name: 'Pineapple Villa',
        creationDate: DateTime.now(),
        memberships: [],
      );

      await tester.pumpWidget(buildTabsHarness(
        testContext: ctx,
        householdProvider: householdProv,
        child: const UserScreen(),
      ));

      await tester.pumpAndSettle();

      expect(find.textContaining('Net cash flow'), findsOneWidget);
      expect(find.text('€85.50'), findsOneWidget);
      expect(find.text('Pending Tasks:'), findsOneWidget);
      expect(find.text('5'), findsOneWidget);
      expect(find.text('Expired Tasks:'), findsOneWidget);
      expect(find.text('2'), findsOneWidget);
      expect(find.widgetWithText(AppButton, AppStrings.leaveHousehold), findsOneWidget);
    });

    testWidgets('switches to edit mode and cancels edit mode', (tester) async {
      tester.view.physicalSize = const Size(1200, 1600);
      tester.view.devicePixelRatio = 1.0;
      addTearDown(tester.view.resetPhysicalSize);

      final ctx = createTestContext();
      configureMockAdapter(ctx.adapter);

      await tester.pumpWidget(buildTabsHarness(
        testContext: ctx,
        child: const UserScreen(),
      ));

      await tester.pumpAndSettle();

      // Tap Edit
      await tester.tap(find.widgetWithText(AppButton, AppStrings.edit));
      await tester.pumpAndSettle();

      expect(find.widgetWithText(AppButton, AppStrings.cancel), findsOneWidget);
      expect(find.widgetWithText(AppButton, AppStrings.save), findsOneWidget);

      // Tap Cancel
      await tester.tap(find.widgetWithText(AppButton, AppStrings.cancel));
      await tester.pumpAndSettle();

      expect(find.widgetWithText(AppButton, AppStrings.edit), findsOneWidget);
    });
  });
}
