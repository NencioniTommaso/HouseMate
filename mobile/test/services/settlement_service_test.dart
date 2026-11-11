import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/core/network/api_exception.dart';
import 'package:mobile/services/settlement_service.dart';
import 'package:mobile/shared/dto/expense/request/settlement_create_request_dto.dart';
import 'package:mobile/shared/dto/expense/request/transaction_filter_request_dto.dart';
import 'package:mobile/shared/enums/user_transaction_role.dart';
import 'package:mobile/shared/utils/types/date_range.dart';

import 'test_helpers.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  late TestContext context;
  late SettlementService settlementService;

  setUp(() {
    context = createTestContext(defaultToken: 'jwt-settlement-token');
    settlementService = SettlementService(context.apiClient);
  });

  group('SettlementService - settleDebt', () {
    final settleReq = SettlementCreateRequestDTO(
      debtId: 'debt-55',
      creditorId: 'cred-1',
      amount: 35.0,
      description: 'Dinner settlement',
    );

    final mockResponseJson = {
      'settlementId': 'stl-101',
      'userTransactionRole': 'DEBTOR',
      'involvedId': 'cred-1',
      'involvedName': 'Jane',
      'amount': 35.0,
      'date': '2026-11-10T15:00:00.000Z',
      'description': 'Dinner settlement',
      'householdId': 'h-1',
    };

    test('settles debt via POST /settlements/:debtId and returns SettlementResponseDTO', () async {
      context.adapter.reply(200, mockResponseJson);

      final result = await settlementService.settleDebt('debt-55', settleReq);

      final req = context.adapter.lastRequest!;
      expect(req.method, 'POST');
      expect(req.path, '/settlements/debt-55');
      expect(req.data, settleReq.toJson());

      expect(result.settlementId, 'stl-101');
      expect(result.amount, 35.0);
      expect(result.description, 'Dinner settlement');
      expect(result.userTransactionRole, UserTransactionRole.debtor);
    });

    test('throws ApiException on 400 Bad Request', () async {
      context.adapter.reply(400, {'message': 'Settlement amount exceeds debt'});

      expect(
        () => settlementService.settleDebt('debt-55', settleReq),
        throwsA(isA<ApiException>().having((e) => e.statusCode, 'statusCode', 400)),
      );
    });

    test('throws ApiException on timeout', () async {
      context.adapter.replyError(DioExceptionType.connectionTimeout);

      expect(
        () => settlementService.settleDebt('debt-55', settleReq),
        throwsA(isA<ApiException>()),
      );
    });
  });

  group('SettlementService - getFilteredSettlements', () {
    final filter = TransactionFilterRequestDTO(
      householdId: 'h-1',
      userTransactionRole: UserTransactionRole.debtor,
      description: 'repayment',
      dateRange: DateRange(
        startDate: DateTime.parse('2026-11-01T00:00:00.000Z'),
        endDate: DateTime.parse('2026-11-30T23:59:59.000Z'),
      ),
    );

    final mockListJson = [
      {
        'settlementId': 'stl-1',
        'userTransactionRole': 'DEBTOR',
        'involvedId': 'cred-1',
        'involvedName': 'Jane',
        'amount': 20.0,
        'date': '2026-11-05T12:00:00.000Z',
        'description': 'repayment for coffee',
        'householdId': 'h-1',
      }
    ];

    test('passes filter parameters and returns list of SettlementResponseDTO', () async {
      context.adapter.reply(200, mockListJson);

      final result = await settlementService.getFilteredSettlements(filter);

      final req = context.adapter.lastRequest!;
      expect(req.method, 'GET');
      expect(req.path, '/settlements');
      expect(req.queryParameters['householdId'], 'h-1');
      expect(req.queryParameters['userTransactionRole'], 'DEBTOR');
      expect(req.queryParameters['description'], 'repayment');
      expect(
        req.queryParameters['dateRange.startDate'],
        DateTime.parse('2026-11-01T00:00:00.000Z').toIso8601String(),
      );
      expect(
        req.queryParameters['dateRange.endDate'],
        DateTime.parse('2026-11-30T23:59:59.000Z').toIso8601String(),
      );

      expect(result.length, 1);
      expect(result.first.settlementId, 'stl-1');
      expect(result.first.amount, 20.0);
    });

    test('handles empty filter without query parameters', () async {
      context.adapter.reply(200, <dynamic>[]);

      final result = await settlementService.getFilteredSettlements(TransactionFilterRequestDTO());

      final req = context.adapter.lastRequest!;
      expect(req.queryParameters.isEmpty, isTrue);
      expect(result, isEmpty);
    });

    test('throws ApiException on 500 server error', () async {
      context.adapter.reply(500, {'message': 'Internal Server Error'});

      expect(
        () => settlementService.getFilteredSettlements(filter),
        throwsA(isA<ApiException>().having((e) => e.statusCode, 'statusCode', 500)),
      );
    });
  });
}
