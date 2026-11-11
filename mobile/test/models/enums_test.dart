import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/shared/dto/chore/request/chore_status_update_request_dto.dart';
import 'package:mobile/shared/dto/expense/request/debt_filter_request_dto.dart';
import 'package:mobile/shared/dto/expense/request/expense_create_request.dart';
import 'package:mobile/shared/dto/items/response/shopping_list_response_dto.dart';
import 'package:mobile/shared/enums/chore_status.dart';
import 'package:mobile/shared/enums/expense_split_type.dart';
import 'package:mobile/shared/enums/message_type.dart';
import 'package:mobile/shared/enums/shopping_list_status.dart';
import 'package:mobile/shared/enums/user_transaction_role.dart';

void main() {
  group('ChoreStatus Enum', () {
    test('has expected values and count', () {
      expect(ChoreStatus.values.length, 3);
      expect(ChoreStatus.values, containsAll([
        ChoreStatus.pending,
        ChoreStatus.completed,
        ChoreStatus.overdue,
      ]));
    });

    test('serializes and deserializes via DTO', () {
      final mapping = {
        ChoreStatus.pending: 'PENDING',
        ChoreStatus.completed: 'COMPLETED',
        ChoreStatus.overdue: 'OVERDUE',
      };

      for (final entry in mapping.entries) {
        final dto = ChoreStatusUpdateRequestDTO(newStatus: entry.key);
        final json = dto.toJson();
        expect(json['newStatus'], entry.value);

        final deserialized = ChoreStatusUpdateRequestDTO.fromJson(json);
        expect(deserialized.newStatus, entry.key);
      }
    });
  });

  group('ExpenseSplitType Enum', () {
    test('has expected values and count', () {
      expect(ExpenseSplitType.values.length, 4);
      expect(ExpenseSplitType.values, containsAll([
        ExpenseSplitType.equalSplit,
        ExpenseSplitType.shares,
        ExpenseSplitType.exactAmount,
        ExpenseSplitType.adjustment,
      ]));
    });

    test('serializes and deserializes via DTO', () {
      final mapping = {
        ExpenseSplitType.equalSplit: 'EQUAL_SPLIT',
        ExpenseSplitType.shares: 'SHARES',
        ExpenseSplitType.exactAmount: 'EXACT_AMOUNT',
        ExpenseSplitType.adjustment: 'ADJUSTMENT',
      };

      for (final entry in mapping.entries) {
        final dto = ExpenseCreateRequestDTO(
          description: 'test',
          amount: 10,
          splitType: entry.key,
          shares: [],
        );
        final json = dto.toJson();
        expect(json['splitType'], entry.value);

        final deserialized = ExpenseCreateRequestDTO.fromJson(json);
        expect(deserialized.splitType, entry.key);
      }
    });
  });

  group('MessageType Enum', () {
    test('has expected values and count', () {
      expect(MessageType.values.length, 3);
      expect(MessageType.values, containsAll([
        MessageType.success,
        MessageType.error,
        MessageType.info,
      ]));
    });

    test('names match Dart convention', () {
      expect(MessageType.success.name, 'success');
      expect(MessageType.error.name, 'error');
      expect(MessageType.info.name, 'info');
    });
  });

  group('ShoppingListStatus Enum', () {
    test('has expected values and count', () {
      expect(ShoppingListStatus.values.length, 3);
      expect(ShoppingListStatus.values, containsAll([
        ShoppingListStatus.notStarted,
        ShoppingListStatus.inProgress,
        ShoppingListStatus.completed,
      ]));
    });

    test('serializes and deserializes via DTO', () {
      final mapping = {
        ShoppingListStatus.notStarted: 'NOT_STARTED',
        ShoppingListStatus.inProgress: 'IN_PROGRESS',
        ShoppingListStatus.completed: 'COMPLETED',
      };

      for (final entry in mapping.entries) {
        final json = {
          'id': 's1',
          'name': 'list',
          'items': <Map<String, dynamic>>[],
          'status': entry.value,
          'householdId': 'h1',
          'creationDate': DateTime.now().toIso8601String(),
        };

        final dto = ShoppingListResponseDTO.fromJson(json);
        expect(dto.status, entry.key);
        expect(dto.toJson()['status'], entry.value);
      }
    });
  });

  group('UserTransactionRole Enum', () {
    test('has expected values and count', () {
      expect(UserTransactionRole.values.length, 3);
      expect(UserTransactionRole.values, containsAll([
        UserTransactionRole.creditor,
        UserTransactionRole.debtor,
        UserTransactionRole.all,
      ]));
    });

    test('serializes and deserializes via DTO', () {
      final mapping = {
        UserTransactionRole.creditor: 'CREDITOR',
        UserTransactionRole.debtor: 'DEBTOR',
        UserTransactionRole.all: 'ALL',
      };

      for (final entry in mapping.entries) {
        final dto = DebtFilterRequestDTO(userTransactionRole: entry.key);
        final json = dto.toJson();
        expect(json['userTransactionRole'], entry.value);

        final deserialized = DebtFilterRequestDTO.fromJson(json);
        expect(deserialized.userTransactionRole, entry.key);
      }
    });
  });
}
