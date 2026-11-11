import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/core/constants/app_strings.dart';
import 'package:mobile/ui/widgets/debt_item_element.dart';
import 'package:mobile/ui/widgets/expense_item_element.dart';
import 'package:mobile/ui/widgets/shared/app_button.dart';
import 'package:mobile/ui/widgets/shared/app_header.dart';

import 'integration_test_helpers.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  group('Expenses and Settlements Integration Flows', () {
    testWidgets('navigate to Expenses tab, view summary and create expense flow', (tester) async {
      tester.view.physicalSize = const Size(1200, 1600);
      tester.view.devicePixelRatio = 1.0;
      addTearDown(tester.view.resetPhysicalSize);

      final ctx = createIntegrationTestContext(
        authenticated: true,
        withHousehold: true,
      );

      await tester.pumpWidget(createIntegrationAppHarness(context: ctx));
      await tester.pumpAndSettle();

      // Switch to Expenses tab (index 2)
      await tester.tap(find.text('Expenses'));
      await tester.pumpAndSettle();

      // Verify Expenses dashboard
      expect(find.widgetWithText(AppHeader, AppStrings.expensesTitle), findsOneWidget);
      expect(find.text(AppStrings.youOwe), findsOneWidget);
      expect(find.text(AppStrings.youAreOwed), findsOneWidget);
      expect(find.byType(ExpenseItemElement), findsOneWidget);
      expect(find.text('Groceries'), findsOneWidget);

      // Tap + New expense button
      await tester.tap(find.widgetWithText(AppButton, AppStrings.newLabel));
      await tester.pumpAndSettle();

      // Verify Create Expense Sheet opened
      expect(find.text('Add Expense'), findsOneWidget);
      expect(find.widgetWithText(AppButton, 'Create'), findsOneWidget);

      // Fill in description and amount
      final textFields = find.byType(TextField);
      await tester.enterText(textFields.first, 'Electricity Bill');
      await tester.enterText(textFields.at(1), '60');
      await tester.pump();

      // Submit new expense
      await tester.tap(find.widgetWithText(AppButton, 'Create'));
      await tester.pumpAndSettle();

      // Verify the new expense is listed
      expect(find.text('Electricity Bill'), findsOneWidget);
    });

    testWidgets('view debts and settle debt flow', (tester) async {
      tester.view.physicalSize = const Size(1200, 1600);
      tester.view.devicePixelRatio = 1.0;
      addTearDown(tester.view.resetPhysicalSize);

      final ctx = createIntegrationTestContext(
        authenticated: true,
        withHousehold: true,
      );

      await tester.pumpWidget(createIntegrationAppHarness(context: ctx));
      await tester.pumpAndSettle();

      // Switch to Expenses tab
      await tester.tap(find.text('Expenses'));
      await tester.pumpAndSettle();

      // Tap You Owe card to open debts sheet
      await tester.tap(find.text(AppStrings.youOwe));
      await tester.pumpAndSettle();

      // Verify Your Debts sheet
      expect(find.text('Your Debts'), findsOneWidget);
      expect(find.byType(DebtItemElement), findsOneWidget);
      expect(find.textContaining('Bob Jones'), findsOneWidget);
      expect(find.widgetWithText(AppButton, 'Pay'), findsOneWidget);

      // Tap Pay to open settle debt sheet
      await tester.tap(find.widgetWithText(AppButton, 'Pay'));
      await tester.pumpAndSettle();

      // Verify Settle Debt sheet
      expect(find.text('Settle Debt'), findsNWidgets(2)); // Header + Button
      expect(find.widgetWithText(ElevatedButton, 'Settle Debt'), findsOneWidget);

      // Tap Settle Debt
      await tester.tap(find.widgetWithText(ElevatedButton, 'Settle Debt'));
      await tester.pumpAndSettle();

      // Verify debt is settled (modal closed and debts cleared in state)
      expect(ctx.state.debts, isEmpty);
      expect(ctx.uiService.successMessages, contains('Debt settled successfully'));
    });
  });
}
