import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/core/network/api_exception.dart';
import 'package:mobile/services/debt_service.dart';
import 'package:mobile/shared/dto/expense/request/debt_filter_request_dto.dart';
import 'package:mobile/shared/enums/user_transaction_role.dart';

import 'test_helpers.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  late TestContext context;
  late DebtService debtService;

  setUp(() {
    context = createTestContext(defaultToken: 'jwt-debt-token');
    debtService = DebtService(context.apiClient);
  });

  group('DebtService - getFilteredDebts', () {
    test('sends query parameters and returns List<DebtResponseDTO>', () async {
      final mockData = [
        {
          'debtId': 'debt-1',
          'userTransactionRole': 'DEBTOR',
          'involvedId': 'user-2',
          'involvedName': 'Alice',
          'amount': 25.50,
        },
        {
          'debtId': 'debt-2',
          'userTransactionRole': 'CREDITOR',
          'involvedId': 'user-3',
          'involvedName': 'Bob',
          'amount': 40.0,
        },
      ];
      context.adapter.reply(200, mockData);

      final filter = DebtFilterRequestDTO(
        userTransactionRole: UserTransactionRole.debtor,
        involvedId: 'user-2',
      );

      final result = await debtService.getFilteredDebts(filter);

      final req = context.adapter.lastRequest!;
      expect(req.method, 'GET');
      expect(req.path, '/debts');
      expect(req.queryParameters['userTransactionRole'], 'DEBTOR');
      expect(req.queryParameters['involvedId'], 'user-2');
      expect(req.headers['Authorization'], 'Bearer jwt-debt-token');

      expect(result.length, 2);
      expect(result[0].debtId, 'debt-1');
      expect(result[0].amount, 25.50);
      expect(result[1].debtId, 'debt-2');
      expect(result[1].amount, 40.0);
    });

    test('omits involvedId query param when null', () async {
      context.adapter.reply(200, <dynamic>[]);

      final filter = DebtFilterRequestDTO(
        userTransactionRole: UserTransactionRole.all,
      );

      final result = await debtService.getFilteredDebts(filter);

      final req = context.adapter.lastRequest!;
      expect(req.queryParameters['userTransactionRole'], 'ALL');
      expect(req.queryParameters.containsKey('involvedId'), isFalse);
      expect(result, isEmpty);
    });

    test('throws ApiException on error', () async {
      context.adapter.reply(400, {'message': 'Invalid role'});

      final filter = DebtFilterRequestDTO(userTransactionRole: UserTransactionRole.creditor);

      expect(
        () => debtService.getFilteredDebts(filter),
        throwsA(isA<ApiException>().having((e) => e.statusCode, 'statusCode', 400)),
      );
    });
  });

  group('DebtService - getCurrentUserDebtOverview', () {
    test('returns DebtOverviewResponseDTO', () async {
      final mockOverview = {
        'totalOwedByMe': 75.0,
        'totalOwedToMe': 150.25,
      };
      context.adapter.reply(200, mockOverview);

      final result = await debtService.getCurrentUserDebtOverview();

      final req = context.adapter.lastRequest!;
      expect(req.method, 'GET');
      expect(req.path, '/debts/me');

      expect(result.totalOwedByMe, 75.0);
      expect(result.totalOwedToMe, 150.25);
    });

    test('throws ApiException on 401', () async {
      context.adapter.reply(401, {'error': 'Unauthorized'});

      expect(
        () => debtService.getCurrentUserDebtOverview(),
        throwsA(isA<ApiException>().having((e) => e.statusCode, 'statusCode', 401)),
      );
    });
  });

  group('DebtService - deleteDebt', () {
    test('sends DELETE /debts/:debtId', () async {
      context.adapter.reply(200, null);

      await debtService.deleteDebt('debt-123');

      final req = context.adapter.lastRequest!;
      expect(req.method, 'DELETE');
      expect(req.path, '/debts/debt-123');
    });

    test('throws ApiException on 404', () async {
      context.adapter.reply(404, {'message': 'Debt not found'});

      expect(
        () => debtService.deleteDebt('debt-999'),
        throwsA(isA<ApiException>().having((e) => e.statusCode, 'statusCode', 404)),
      );
    });

    test('throws ApiException on timeout', () async {
      context.adapter.replyError(DioExceptionType.connectionTimeout);

      expect(
        () => debtService.deleteDebt('debt-timeout'),
        throwsA(isA<ApiException>()),
      );
    });
  });
}
