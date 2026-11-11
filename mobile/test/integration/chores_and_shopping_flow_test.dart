import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/core/constants/app_strings.dart';
import 'package:mobile/ui/widgets/chore_assignment_item_element.dart';
import 'package:mobile/ui/widgets/shared/app_button.dart';

import 'integration_test_helpers.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  group('Chores and Shopping Integration Flows', () {
    testWidgets('create chore flow from household screen', (tester) async {
      tester.view.physicalSize = const Size(1200, 1600);
      tester.view.devicePixelRatio = 1.0;
      addTearDown(tester.view.resetPhysicalSize);

      final ctx = createIntegrationTestContext(
        authenticated: true,
        withHousehold: true,
      );

      await tester.pumpWidget(createIntegrationAppHarness(context: ctx));
      await tester.pumpAndSettle();

      // In Household screen, tap Chores List button
      await tester.tap(find.widgetWithText(AppButton, AppStrings.choresList));
      await tester.pumpAndSettle();

      // Verify Chores List sheet
      expect(find.text('Chores List'), findsNWidgets(2)); // Button + Sheet title
      expect(find.text('Clean Kitchen'), findsOneWidget);
      expect(find.widgetWithText(ElevatedButton, '+ Create New Chore'), findsOneWidget);

      // Tap + Create New Chore
      await tester.tap(find.widgetWithText(ElevatedButton, '+ Create New Chore'));
      await tester.pumpAndSettle();

      // Verify Create Chore sheet
      expect(find.text('New Chore'), findsOneWidget);
      expect(find.widgetWithText(ElevatedButton, 'Create'), findsOneWidget);

      // Enter chore details
      final textFields = find.byType(TextField);
      await tester.enterText(textFields.first, 'Take out Trash');
      await tester.enterText(textFields.at(1), '3');
      await tester.pump();

      // Submit chore
      await tester.tap(find.widgetWithText(ElevatedButton, 'Create'));
      await tester.pumpAndSettle();

      // Verify the new chore is in the list
      expect(find.text('Take out Trash'), findsOneWidget);
    });

    testWidgets('toggle chore assignment completion in Assignments tab', (tester) async {
      tester.view.physicalSize = const Size(1200, 1600);
      tester.view.devicePixelRatio = 1.0;
      addTearDown(tester.view.resetPhysicalSize);

      final ctx = createIntegrationTestContext(
        authenticated: true,
        withHousehold: true,
      );

      await tester.pumpWidget(createIntegrationAppHarness(context: ctx));
      await tester.pumpAndSettle();

      // Switch to Assignments tab (index 1)
      await tester.tap(find.text('Assignments'));
      await tester.pumpAndSettle();

      // Verify today's chore assignment
      expect(find.byType(ChoreAssignmentItemElement), findsOneWidget);
      expect(find.text('Clean Kitchen'), findsOneWidget);
      expect(find.textContaining('Assigned to: Alice'), findsOneWidget);

      // Find the completion checkbox inside ChoreAssignmentItemElement
      final checkbox = find.descendant(
        of: find.byType(ChoreAssignmentItemElement),
        matching: find.byType(Checkbox),
      );
      expect(checkbox, findsOneWidget);

      // Tap checkbox to open completion confirmation dialog
      await tester.tap(checkbox);
      await tester.pumpAndSettle();

      // Confirm dialog appears
      expect(find.text('Complete Chore'), findsOneWidget);
      await tester.tap(find.widgetWithText(ElevatedButton, 'Confirm'));
      await tester.pumpAndSettle();

      // Verify assignment was updated in state
      expect(ctx.state.assignments.first['status'], 'COMPLETED');
    });

    testWidgets('create shopping list and toggle item bought status', (tester) async {
      tester.view.physicalSize = const Size(1200, 1600);
      tester.view.devicePixelRatio = 1.0;
      addTearDown(tester.view.resetPhysicalSize);

      final ctx = createIntegrationTestContext(
        authenticated: true,
        withHousehold: true,
      );

      await tester.pumpWidget(createIntegrationAppHarness(context: ctx));
      await tester.pumpAndSettle();

      // In Household screen, tap Shopping Lists button
      await tester.tap(find.widgetWithText(AppButton, AppStrings.shoppingLists));
      await tester.pumpAndSettle();

      // Verify Shopping Lists sheet
      expect(find.text('Shopping Lists'), findsNWidgets(2)); // Button + Sheet title
      expect(find.text('Weekly Groceries'), findsOneWidget);
      expect(find.widgetWithText(ElevatedButton, '+ New Shopping List'), findsOneWidget);

      // Tap Details on the existing list
      await tester.tap(find.widgetWithText(ElevatedButton, 'Details'));
      await tester.pumpAndSettle();

      // Verify list details sheet
      expect(find.text('Weekly Groceries'), findsNWidgets(2)); // Card + Sheet header
      expect(find.text('Milk'), findsOneWidget);
      expect(find.text('Bread'), findsOneWidget);

      // Toggle Milk (was false -> now true)
      final milkTile = find.widgetWithText(CheckboxListTile, 'Milk');
      expect(milkTile, findsOneWidget);
      await tester.tap(milkTile);
      await tester.pump();

      // Tap Save Changes
      await tester.tap(find.widgetWithText(ElevatedButton, 'Save Changes'));
      await tester.pumpAndSettle();

      // Verify item bought status was updated
      final updatedList = ctx.state.shoppingLists.first;
      final milkItem = (updatedList['items'] as List<dynamic>).first;
      expect(milkItem['isBought'], isTrue);
    });
  });
}
