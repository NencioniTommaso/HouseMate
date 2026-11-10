import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/shared/dto/expense/request/debt_filter_request_dto.dart';
import 'package:mobile/shared/dto/expense/request/expense_create_request.dart';
import 'package:mobile/shared/dto/expense/request/expense_share_request_dto.dart';
import 'package:mobile/shared/dto/expense/request/settlement_create_request_dto.dart';
import 'package:mobile/shared/dto/expense/request/transaction_filter_request_dto.dart';
import 'package:mobile/shared/dto/expense/response/debt_overview_response_dto.dart';
import 'package:mobile/shared/dto/expense/response/debt_response_dto.dart';
import 'package:mobile/shared/dto/expense/response/expense_overview_response_dto.dart';
import 'package:mobile/shared/dto/expense/response/expense_response_dto.dart';
import 'package:mobile/shared/dto/expense/response/expense_share_response_dto.dart';
import 'package:mobile/shared/dto/expense/response/settlement_response_dto.dart';
import 'package:mobile/shared/dto/expense/response/user_net_overview_response_dto.dart';
import 'package:mobile/shared/enums/expense_split_type.dart';
import 'package:mobile/shared/enums/user_transaction_role.dart';
import 'package:mobile/shared/utils/types/date_range.dart';

void main() {
  group('ExpenseOverviewResponseDTO', () {
    test('should serialize and deserialize with num to double conversion', () {
      final dto = ExpenseOverviewResponseDTO(
        totalAmount: 125.50,
        expenseCount: 5,
      );

      final json = dto.toJson();
      expect(json, {
        'totalAmount': 125.50,
        'expenseCount': 5,
      });

      final deserialized = ExpenseOverviewResponseDTO.fromJson({
        'totalAmount': 100, // int num
        'expenseCount': 4,
      });
      expect(deserialized.totalAmount, 100.0);
      expect(deserialized.expenseCount, 4);
    });

    test('should handle null expenseCount', () {
      final json = {'totalAmount': 50.0};
      final dto = ExpenseOverviewResponseDTO.fromJson(json);
      expect(dto.totalAmount, 50.0);
      expect(dto.expenseCount, isNull);
      expect(dto.toJson()['expenseCount'], isNull);
    });
  });

  group('DebtResponseDTO', () {
    test('should serialize and deserialize correctly with int amount', () {
      final json = {
        'debtId': 'debt-101',
        'userTransactionRole': 'CREDITOR',
        'involvedId': 'user-2',
        'involvedName': 'Jane Smith',
        'amount': 75,
      };

      final dto = DebtResponseDTO.fromJson(json);
      expect(dto.debtId, 'debt-101');
      expect(dto.userTransactionRole, UserTransactionRole.creditor);
      expect(dto.involvedId, 'user-2');
      expect(dto.involvedName, 'Jane Smith');
      expect(dto.amount, 75.0);

      final serialized = dto.toJson();
      expect(serialized['amount'], 75.0);
      expect(serialized['userTransactionRole'], 'CREDITOR');
    });
  });

  group('SettlementResponseDTO', () {
    test('should serialize and deserialize with ISO String date', () {
      final date = DateTime.parse('2026-11-10T14:00:00.000Z');
      final dto = SettlementResponseDTO(
        settlementId: 'stl-1',
        userTransactionRole: UserTransactionRole.debtor,
        involvedId: 'user-3',
        involvedName: 'Alice Brown',
        amount: 25.50,
        date: date,
        description: 'Repayment for lunch',
        householdId: 'house-1',
      );

      final json = dto.toJson();
      expect(json['settlementId'], 'stl-1');
      expect(json['userTransactionRole'], 'DEBTOR');
      expect(json['amount'], 25.50);
      expect(json['date'], date.toIso8601String());
      expect(json['description'], 'Repayment for lunch');

      final deserialized = SettlementResponseDTO.fromJson(json);
      expect(deserialized.settlementId, dto.settlementId);
      expect(deserialized.date, date);
      expect(deserialized.amount, 25.50);
    });

    test('should deserialize with list-format date', () {
      final json = {
        'settlementId': 'stl-2',
        'userTransactionRole': 'ALL',
        'involvedId': 'user-4',
        'involvedName': 'Bob',
        'amount': 30,
        'date': [2026, 8, 15, 10, 0, 0],
        'householdId': 'house-1',
      };

      final dto = SettlementResponseDTO.fromJson(json);
      expect(dto.date, DateTime(2026, 8, 15, 10, 0, 0));
      expect(dto.amount, 30.0);
      expect(dto.description, isNull);
    });

    test('should deserialize with null date and null description', () {
      final json = {
        'settlementId': 'stl-3',
        'userTransactionRole': 'CREDITOR',
        'involvedId': 'user-5',
        'involvedName': 'Charlie',
        'amount': 10.0,
        'date': null,
        'householdId': 'house-1',
      };

      final dto = SettlementResponseDTO.fromJson(json);
      expect(dto.date, isNull);
      expect(dto.description, isNull);
    });
  });

  group('ExpenseShareResponseDTO', () {
    test('should serialize and deserialize correctly', () {
      final dto = ExpenseShareResponseDTO(
        id: 'share-1',
        userId: 'user-1',
        userFullName: 'Alice Smith',
        amount: 15.75,
      );

      final json = dto.toJson();
      expect(json, {
        'id': 'share-1',
        'userId': 'user-1',
        'userFullName': 'Alice Smith',
        'amount': 15.75,
      });

      final deserialized = ExpenseShareResponseDTO.fromJson({
        'id': 'share-1',
        'userId': 'user-1',
        'userFullName': 'Alice Smith',
        'amount': 15,
      });
      expect(deserialized.amount, 15.0);
    });
  });

  group('ExpenseResponseDTO', () {
    test('should serialize and deserialize with full nested shares and ISO date', () {
      final date = DateTime.parse('2026-11-01T10:00:00.000Z');
      final share = ExpenseShareResponseDTO(
        id: 's-1',
        userId: 'u-1',
        userFullName: 'User One',
        amount: 50.0,
      );

      final dto = ExpenseResponseDTO(
        id: 'exp-1',
        description: 'Groceries',
        date: date,
        amount: 100.0,
        payerId: 'u-1',
        payerFullName: 'User One',
        splitType: ExpenseSplitType.equalSplit,
        householdId: 'h-1',
        shares: [share],
      );

      final json = dto.toJson();
      expect(json['id'], 'exp-1');
      expect(json['splitType'], 'EQUAL_SPLIT');
      expect(json['shares'], [share.toJson()]);

      final deserialized = ExpenseResponseDTO.fromJson(json);
      expect(deserialized.id, dto.id);
      expect(deserialized.splitType, ExpenseSplitType.equalSplit);
      expect(deserialized.shares.length, 1);
      expect(deserialized.shares.first.amount, 50.0);
      expect(deserialized.date, date);
    });

    test('should deserialize with list-format date and empty shares', () {
      final json = {
        'id': 'exp-2',
        'description': 'Hardware store',
        'date': [2026, 10, 5],
        'amount': 42,
        'payerId': 'u-2',
        'payerFullName': 'User Two',
        'splitType': 'SHARES',
        'householdId': 'h-1',
        'shares': <Map<String, dynamic>>[],
      };

      final dto = ExpenseResponseDTO.fromJson(json);
      expect(dto.date, DateTime(2026, 10, 5));
      expect(dto.amount, 42.0);
      expect(dto.splitType, ExpenseSplitType.shares);
      expect(dto.shares, isEmpty);
    });

    test('should deserialize with null date', () {
      final json = {
        'id': 'exp-3',
        'description': 'Online subscription',
        'date': null,
        'amount': 9.99,
        'payerId': 'u-3',
        'payerFullName': 'User Three',
        'splitType': 'EXACT_AMOUNT',
        'householdId': 'h-1',
        'shares': <Map<String, dynamic>>[],
      };

      final dto = ExpenseResponseDTO.fromJson(json);
      expect(dto.date, isNull);
    });
  });

  group('DebtOverviewResponseDTO', () {
    test('should serialize and deserialize correctly', () {
      final dto = DebtOverviewResponseDTO(
        totalOwedByMe: 45.50,
        totalOwedToMe: 120.00,
      );

      final json = dto.toJson();
      expect(json, {
        'totalOwedByMe': 45.50,
        'totalOwedToMe': 120.00,
      });

      final deserialized = DebtOverviewResponseDTO.fromJson(json);
      expect(deserialized.totalOwedByMe, 45.50);
      expect(deserialized.totalOwedToMe, 120.00);
    });
  });

  group('UserNetOverviewResponseDTO', () {
    test('should serialize and deserialize with num casting', () {
      final dto = UserNetOverviewResponseDTO(actualCashFlowAmount: 200.75);
      final json = dto.toJson();
      expect(json, {'actualCashFlowAmount': 200.75});

      final deserialized = UserNetOverviewResponseDTO.fromJson({
        'actualCashFlowAmount': 100,
      });
      expect(deserialized.actualCashFlowAmount, 100.0);
    });
  });

  group('ExpenseCreateRequestDTO and ExpenseShareRequestDTO', () {
    test('should serialize and deserialize correctly', () {
      final share1 = ExpenseShareRequestDTO(userId: 'u-1', share: 2.0);
      final share2 = ExpenseShareRequestDTO(userId: 'u-2', share: null);

      final dto = ExpenseCreateRequestDTO(
        description: 'Dinner',
        amount: 60.0,
        splitType: ExpenseSplitType.shares,
        shares: [share1, share2],
      );

      final json = dto.toJson();
      expect(json['description'], 'Dinner');
      expect(json['amount'], 60.0);
      expect(json['splitType'], 'SHARES');
      expect((json['shares'] as List).length, 2);

      final deserialized = ExpenseCreateRequestDTO.fromJson(json);
      expect(deserialized.description, 'Dinner');
      expect(deserialized.shares[0].userId, 'u-1');
      expect(deserialized.shares[0].share, 2.0);
      expect(deserialized.shares[1].userId, 'u-2');
      expect(deserialized.shares[1].share, isNull);
    });
  });

  group('SettlementCreateRequestDTO', () {
    test('should serialize and deserialize with description present', () {
      final dto = SettlementCreateRequestDTO(
        debtId: 'debt-1',
        creditorId: 'cred-1',
        amount: 50.0,
        description: 'Partial payback',
      );

      final json = dto.toJson();
      expect(json, {
        'debtId': 'debt-1',
        'creditorId': 'cred-1',
        'amount': 50.0,
        'description': 'Partial payback',
      });

      final deserialized = SettlementCreateRequestDTO.fromJson(json);
      expect(deserialized.debtId, 'debt-1');
      expect(deserialized.description, 'Partial payback');
    });

    test('should serialize and deserialize with description null', () {
      final dto = SettlementCreateRequestDTO(
        debtId: 'debt-2',
        creditorId: 'cred-2',
        amount: 25.0,
      );

      final json = dto.toJson();
      expect(json['description'], isNull);

      final deserialized = SettlementCreateRequestDTO.fromJson(json);
      expect(deserialized.description, isNull);
    });
  });

  group('TransactionFilterRequestDTO', () {
    test('should serialize and deserialize with full fields', () {
      final range = DateRange(
        startDate: DateTime.parse('2026-01-01T00:00:00.000Z'),
        endDate: DateTime.parse('2026-06-30T23:59:59.000Z'),
      );
      final dto = TransactionFilterRequestDTO(
        householdId: 'house-1',
        userTransactionRole: UserTransactionRole.creditor,
        dateRange: range,
        description: 'Supermarket',
      );

      final json = dto.toJson();
      expect(json['householdId'], 'house-1');
      expect(json['userTransactionRole'], 'CREDITOR');
      expect(json['description'], 'Supermarket');

      final deserialized = TransactionFilterRequestDTO.fromJson(json);
      expect(deserialized.householdId, 'house-1');
      expect(deserialized.userTransactionRole, UserTransactionRole.creditor);
      expect(deserialized.description, 'Supermarket');
      expect(deserialized.dateRange?.startDate, range.startDate);
    });

    test('should serialize and deserialize with all null fields', () {
      final dto = TransactionFilterRequestDTO();
      final json = dto.toJson();
      expect(json['householdId'], isNull);
      expect(json['userTransactionRole'], isNull);
      expect(json['dateRange'], isNull);
      expect(json['description'], isNull);

      final deserialized = TransactionFilterRequestDTO.fromJson({});
      expect(deserialized.householdId, isNull);
      expect(deserialized.userTransactionRole, isNull);
      expect(deserialized.dateRange, isNull);
      expect(deserialized.description, isNull);
    });
  });

  group('DebtFilterRequestDTO', () {
    test('should serialize and deserialize correctly', () {
      final dto = DebtFilterRequestDTO(
        userTransactionRole: UserTransactionRole.debtor,
        involvedId: 'user-7',
      );

      final json = dto.toJson();
      expect(json, {
        'userTransactionRole': 'DEBTOR',
        'involvedId': 'user-7',
      });

      final deserialized = DebtFilterRequestDTO.fromJson(json);
      expect(deserialized.userTransactionRole, UserTransactionRole.debtor);
      expect(deserialized.involvedId, 'user-7');
    });

    test('should handle null involvedId', () {
      final dto = DebtFilterRequestDTO(
        userTransactionRole: UserTransactionRole.all,
      );

      final json = dto.toJson();
      expect(json['involvedId'], isNull);

      final deserialized = DebtFilterRequestDTO.fromJson(json);
      expect(deserialized.userTransactionRole, UserTransactionRole.all);
      expect(deserialized.involvedId, isNull);
    });
  });
}
