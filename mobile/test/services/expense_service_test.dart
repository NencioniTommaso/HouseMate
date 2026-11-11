import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/core/network/api_exception.dart';
import 'package:mobile/services/expense_service.dart';
import 'package:mobile/shared/dto/expense/request/expense_create_request.dart';
import 'package:mobile/shared/dto/expense/request/expense_share_request_dto.dart';
import 'package:mobile/shared/dto/expense/request/transaction_filter_request_dto.dart';
import 'package:mobile/shared/enums/expense_split_type.dart';
import 'package:mobile/shared/enums/user_transaction_role.dart';
import 'package:mobile/shared/utils/types/date_range.dart';

import 'test_helpers.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  late TestContext context;
  late ExpenseService expenseService;

  setUp(() {
    context = createTestContext(defaultToken: 'jwt-expense-token');
    expenseService = ExpenseService(context.apiClient);
  });

  group('ExpenseService - createExpense', () {
    final createReq = ExpenseCreateRequestDTO(
      description: 'Dinner with flatmates',
      amount: 45.0,
      splitType: ExpenseSplitType.equalSplit,
      shares: [
        ExpenseShareRequestDTO(userId: 'u-1', share: 15.0),
        ExpenseShareRequestDTO(userId: 'u-2', share: 15.0),
        ExpenseShareRequestDTO(userId: 'u-3', share: 15.0),
      ],
    );

    final mockResponse = {
      'id': 'exp-100',
      'description': 'Dinner with flatmates',
      'date': '2026-11-10T19:00:00.000Z',
      'amount': 45.0,
      'payerId': 'u-1',
      'payerFullName': 'John Doe',
      'splitType': 'EQUAL_SPLIT',
      'householdId': 'h-1',
      'shares': [
        {'id': 's-1', 'userId': 'u-1', 'userFullName': 'John Doe', 'amount': 15.0},
        {'id': 's-2', 'userId': 'u-2', 'userFullName': 'Jane Smith', 'amount': 15.0},
        {'id': 's-3', 'userId': 'u-3', 'userFullName': 'Bob Brown', 'amount': 15.0},
      ],
    };

    test('creates expense via POST /expenses and returns ExpenseResponseDTO', () async {
      context.adapter.reply(200, mockResponse);

      final result = await expenseService.createExpense(createReq);

      final req = context.adapter.lastRequest!;
      expect(req.method, 'POST');
      expect(req.path, '/expenses');
      expect(req.data, createReq.toJson());

      expect(result.id, 'exp-100');
      expect(result.amount, 45.0);
      expect(result.shares.length, 3);
      expect(result.splitType, ExpenseSplitType.equalSplit);
    });

    test('throws ApiException on 400 Bad Request', () async {
      context.adapter.reply(400, {'message': 'Invalid shares calculation'});

      expect(
        () => expenseService.createExpense(createReq),
        throwsA(isA<ApiException>().having((e) => e.statusCode, 'statusCode', 400)),
      );
    });

    test('throws ApiException on timeout', () async {
      context.adapter.replyError(DioExceptionType.connectionTimeout);

      expect(
        () => expenseService.createExpense(createReq),
        throwsA(isA<ApiException>()),
      );
    });
  });

  group('ExpenseService - getFilteredExpenses', () {
    final filter = TransactionFilterRequestDTO(
      householdId: 'h-1',
      userTransactionRole: UserTransactionRole.creditor,
      description: 'groceries',
      dateRange: DateRange(
        startDate: DateTime.parse('2026-11-01T00:00:00.000Z'),
        endDate: DateTime.parse('2026-11-30T23:59:59.000Z'),
      ),
    );

    final mockList = [
      {
        'id': 'exp-1',
        'description': 'Weekly groceries',
        'date': '2026-11-05T10:00:00.000Z',
        'amount': 60.50,
        'payerId': 'u-1',
        'payerFullName': 'Alice',
        'splitType': 'EQUAL_SPLIT',
        'householdId': 'h-1',
        'shares': <Map<String, dynamic>>[],
      }
    ];

    test('passes query parameters and parses list of ExpenseResponseDTO', () async {
      context.adapter.reply(200, mockList);

      final result = await expenseService.getFilteredExpenses(filter);

      final req = context.adapter.lastRequest!;
      expect(req.method, 'GET');
      expect(req.path, '/expenses');
      expect(req.queryParameters['householdId'], 'h-1');
      expect(req.queryParameters['userTransactionRole'], 'CREDITOR');
      expect(req.queryParameters['description'], 'groceries');
      expect(
        req.queryParameters['dateRange.startDate'],
        DateTime.parse('2026-11-01T00:00:00.000Z').toIso8601String(),
      );
      expect(
        req.queryParameters['dateRange.endDate'],
        DateTime.parse('2026-11-30T23:59:59.000Z').toIso8601String(),
      );

      expect(result.length, 1);
      expect(result.first.id, 'exp-1');
      expect(result.first.amount, 60.50);
    });

    test('omits optional query parameters when not specified', () async {
      context.adapter.reply(200, <dynamic>[]);

      final emptyFilter = TransactionFilterRequestDTO();
      final result = await expenseService.getFilteredExpenses(emptyFilter);

      final req = context.adapter.lastRequest!;
      expect(req.queryParameters.isEmpty, isTrue);
      expect(result, isEmpty);
    });

    test('throws ApiException on 500 error', () async {
      context.adapter.reply(500, {'message': 'Database error'});

      expect(
        () => expenseService.getFilteredExpenses(filter),
        throwsA(isA<ApiException>().having((e) => e.statusCode, 'statusCode', 500)),
      );
    });
  });

  group('ExpenseService - getCurrentMonthExpenseOverview', () {
    test('returns ExpenseOverviewResponseDTO', () async {
      final mockData = {
        'totalAmount': 240.50,
        'expenseCount': 8,
      };
      context.adapter.reply(200, mockData);

      final result = await expenseService.getCurrentMonthExpenseOverview();

      final req = context.adapter.lastRequest!;
      expect(req.method, 'GET');
      expect(req.path, '/expenses/overview');

      expect(result.totalAmount, 240.50);
      expect(result.expenseCount, 8);
    });

    test('throws ApiException on 401 Unauthorized', () async {
      context.adapter.reply(401, {'error': 'Token expired'});

      expect(
        () => expenseService.getCurrentMonthExpenseOverview(),
        throwsA(isA<ApiException>().having((e) => e.statusCode, 'statusCode', 401)),
      );
    });
  });

  group('ExpenseService - getCurrentMonthUserNetOverview', () {
    test('returns UserNetOverviewResponseDTO', () async {
      final mockData = {
        'actualCashFlowAmount': -35.20,
      };
      context.adapter.reply(200, mockData);

      final result = await expenseService.getCurrentMonthUserNetOverview();

      final req = context.adapter.lastRequest!;
      expect(req.method, 'GET');
      expect(req.path, '/expenses/me');

      expect(result.actualCashFlowAmount, -35.20);
    });

    test('throws ApiException on server error', () async {
      context.adapter.reply(503, {'message': 'Service unavailable'});

      expect(
        () => expenseService.getCurrentMonthUserNetOverview(),
        throwsA(isA<ApiException>().having((e) => e.statusCode, 'statusCode', 503)),
      );
    });
  });
}
