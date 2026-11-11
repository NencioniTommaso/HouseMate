import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/core/theme/app_colors.dart';
import 'package:mobile/shared/dto/chore/response/chore_assignment_response_dto.dart';
import 'package:mobile/shared/dto/chore/response/chore_response_dto.dart';
import 'package:mobile/shared/dto/expense/response/debt_response_dto.dart';
import 'package:mobile/shared/dto/expense/response/expense_response_dto.dart';
import 'package:mobile/shared/dto/expense/response/expense_share_response_dto.dart';
import 'package:mobile/shared/dto/expense/response/settlement_response_dto.dart';
import 'package:mobile/shared/dto/user/response/user_response_dto.dart';
import 'package:mobile/shared/enums/chore_status.dart';
import 'package:mobile/shared/enums/expense_split_type.dart';
import 'package:mobile/shared/enums/user_transaction_role.dart';
import 'package:mobile/ui/widgets/chore_assignment_item_element.dart';
import 'package:mobile/ui/widgets/chore_item_element.dart';
import 'package:mobile/ui/widgets/debt_item_element.dart';
import 'package:mobile/ui/widgets/expense_item_element.dart';
import 'package:mobile/ui/widgets/member_item_element.dart';
import 'package:mobile/ui/widgets/member_split_box.dart';
import 'package:mobile/ui/widgets/settlement_item_element.dart';

Widget wrapWidget(Widget child) {
  return MaterialApp(
    home: Scaffold(
      body: SingleChildScrollView(child: child),
    ),
  );
}

void main() {
  final sampleUser = UserResponseDTO(
    id: 'u-1',
    name: 'Alice',
    surname: 'Smith',
    email: 'alice@example.com',
    iban: 'NL91ABNA0417164300',
    paymentLink: 'https://pay.me/alice',
  );

  group('ChoreAssignmentItemElement', () {
    final assignment = ChoreAssignmentResponseDTO(
      assignmentId: 'a-1',
      choreId: 'c-1',
      choreDescription: 'Wash Dishes',
      assignedUser: sampleUser,
      dueDate: DateTime(2026, 12, 1, 10, 0),
      status: ChoreStatus.pending,
    );

    testWidgets('renders chore description and assignee name', (tester) async {
      await tester.pumpWidget(
        wrapWidget(
          ChoreAssignmentItemElement(
            assignment: assignment,
            currentUserId: 'u-1',
            isAdmin: false,
          ),
        ),
      );

      expect(find.text('Wash Dishes'), findsOneWidget);
      expect(find.textContaining('Assigned to: Alice'), findsOneWidget);
      expect(find.text('PENDING'), findsOneWidget);
    });

    testWidgets('triggers onStatusToggle callback when tapped', (tester) async {
      var toggled = false;
      await tester.pumpWidget(
        wrapWidget(
          ChoreAssignmentItemElement(
            assignment: assignment,
            currentUserId: 'u-1',
            isAdmin: false,
            onStatusToggle: () => toggled = true,
          ),
        ),
      );

      // Tap Checkbox to toggle status
      await tester.tap(find.byType(Checkbox));
      await tester.pump();

      expect(toggled, isTrue);
    });

    testWidgets('wraps in Dismissible when isAdmin is true', (tester) async {
      await tester.pumpWidget(
        wrapWidget(
          ChoreAssignmentItemElement(
            assignment: assignment,
            currentUserId: 'u-1',
            isAdmin: true,
            onDelete: () {},
          ),
        ),
      );

      expect(find.byType(Dismissible), findsOneWidget);
    });
  });

  group('ChoreItemElement', () {
    final chore = ChoreResponseDTO(
      id: 'c-1',
      description: 'Mow the lawn',
      frequencyDays: 7,
    );

    testWidgets('renders description and frequency formatting', (tester) async {
      await tester.pumpWidget(
        wrapWidget(
          ChoreItemElement(
            chore: chore,
            onDelete: () {},
            isAdminMode: false,
          ),
        ),
      );

      expect(find.text('Mow the lawn'), findsOneWidget);
      expect(find.text('Frequency: every 7 days'), findsOneWidget);
      expect(find.byIcon(Icons.delete_outline), findsNothing);
    });

    testWidgets('shows and handles delete button when isAdminMode is true', (tester) async {
      var deleted = false;
      await tester.pumpWidget(
        wrapWidget(
          ChoreItemElement(
            chore: chore,
            onDelete: () => deleted = true,
            isAdminMode: true,
          ),
        ),
      );

      expect(find.text('Delete'), findsOneWidget);
      await tester.tap(find.text('Delete'));
      await tester.pump();

      expect(deleted, isTrue);
    });
  });

  group('DebtItemElement', () {
    testWidgets('renders creditor debt title ("owes you") without Pay button', (tester) async {
      final debt = DebtResponseDTO(
        debtId: 'd-1',
        userTransactionRole: UserTransactionRole.creditor,
        involvedId: 'u-2',
        involvedName: 'Bob',
        amount: 35.50,
      );

      await tester.pumpWidget(
        wrapWidget(
          DebtItemElement(
            debt: debt,
            onPay: (_) {},
          ),
        ),
      );

      expect(find.text('Bob owes you'), findsOneWidget);
      expect(find.text('€35.50'), findsOneWidget);
      expect(find.text('Pay'), findsNothing);
    });

    testWidgets('renders debtor debt title ("Debt to Bob") and handles Pay tap', (tester) async {
      final debt = DebtResponseDTO(
        debtId: 'd-2',
        userTransactionRole: UserTransactionRole.debtor,
        involvedId: 'u-2',
        involvedName: 'Bob',
        amount: 20.00,
      );

      DebtResponseDTO? paidDebt;
      await tester.pumpWidget(
        wrapWidget(
          DebtItemElement(
            debt: debt,
            onPay: (d) => paidDebt = d,
          ),
        ),
      );

      expect(find.text('Debt to Bob'), findsOneWidget);
      expect(find.text('€20.00'), findsOneWidget);
      expect(find.text('Pay'), findsOneWidget);

      await tester.tap(find.text('Pay'));
      await tester.pump();

      expect(paidDebt?.debtId, 'd-2');
    });
  });

  group('ExpenseItemElement', () {
    testWidgets('renders expense details and calculated user share', (tester) async {
      final expense = ExpenseResponseDTO(
        id: 'e-1',
        description: 'Supermarket Groceries',
        date: DateTime(2026, 11, 10, 14, 30),
        amount: 60.00,
        payerId: 'u-2',
        payerFullName: 'Bob Marley',
        splitType: ExpenseSplitType.equalSplit,
        householdId: 'h-1',
        shares: [
          ExpenseShareResponseDTO(id: 's-1', userId: 'u-1', userFullName: 'Alice Smith', amount: 30.00),
          ExpenseShareResponseDTO(id: 's-2', userId: 'u-2', userFullName: 'Bob Marley', amount: 30.00),
        ],
      );

      await tester.pumpWidget(
        wrapWidget(
          ExpenseItemElement(
            expense: expense,
            currentUserId: 'u-1',
          ),
        ),
      );

      expect(find.text('Supermarket Groceries'), findsOneWidget);
      expect(find.text('Paid by Bob Marley'), findsOneWidget);
      expect(find.text('Your share: €30.00'), findsOneWidget);
      expect(find.text('€60.00'), findsOneWidget);
    });
  });

  group('MemberItemElement', () {
    testWidgets('renders member details and "You" badge when isCurrentUser', (tester) async {
      await tester.pumpWidget(
        wrapWidget(
          MemberItemElement(
            member: sampleUser,
            onRemove: () {},
            isAdminMode: true,
            currentUserId: 'u-1',
          ),
        ),
      );

      expect(find.text('Alice Smith'), findsOneWidget);
      expect(find.text('alice@example.com'), findsOneWidget);
      expect(find.text('You'), findsOneWidget);
      expect(find.byIcon(Icons.remove_circle_outline), findsNothing);
    });

    testWidgets('shows remove button for other members when isAdminMode is true', (tester) async {
      final otherUser = UserResponseDTO(
        id: 'u-2',
        name: 'Bob',
        surname: 'Marley',
        email: 'bob@example.com',
      );

      var removed = false;
      await tester.pumpWidget(
        wrapWidget(
          MemberItemElement(
            member: otherUser,
            onRemove: () => removed = true,
            isAdminMode: true,
            currentUserId: 'u-1',
          ),
        ),
      );

      expect(find.text('Bob Marley'), findsOneWidget);
      expect(find.text('Remove'), findsOneWidget);

      await tester.tap(find.text('Remove'));
      await tester.pump();

      expect(removed, isTrue);
    });
  });

  group('MemberSplitBox', () {
    testWidgets('renders member name and checkbox toggle', (tester) async {
      var toggled = false;
      await tester.pumpWidget(
        wrapWidget(
          MemberSplitBox(
            member: sampleUser,
            splitType: ExpenseSplitType.equalSplit,
            calculatedAmount: 15.00,
            isIncluded: true,
            shareCount: 1,
            customAmount: 0.0,
            adjustmentAmount: 0.0,
            onToggle: (val) => toggled = val,
            onSharesChanged: (_) {},
            onAmountChanged: (_) {},
            onAdjustmentChanged: (_) {},
          ),
        ),
      );

      expect(find.text('Alice'), findsOneWidget);
      expect(find.textContaining('15.00'), findsOneWidget);

      await tester.tap(find.byType(Checkbox));
      await tester.pump();

      expect(toggled, isFalse);
    });
  });

  group('SettlementItemElement', () {
    testWidgets('renders settlement with correct direction and amount', (tester) async {
      final settlement = SettlementResponseDTO(
        settlementId: 's-1',
        userTransactionRole: UserTransactionRole.creditor,
        involvedId: 'u-2',
        involvedName: 'Bob',
        amount: 25.00,
        date: DateTime(2026, 11, 5, 12, 0),
        description: 'Settled via Revolut',
        householdId: 'h-1',
      );

      await tester.pumpWidget(
        wrapWidget(
          SettlementItemElement(
            settlement: settlement,
          ),
        ),
      );

      expect(find.text('Received from Bob'), findsOneWidget);
      expect(find.text('€25.00'), findsOneWidget);
      expect(find.text('Settled via Revolut'), findsOneWidget);
    });
  });
}
