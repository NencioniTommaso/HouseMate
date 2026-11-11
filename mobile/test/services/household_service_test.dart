import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/core/network/api_exception.dart';
import 'package:mobile/services/household_service.dart';
import 'package:mobile/shared/dto/household/request/add_member_request_dto.dart';
import 'package:mobile/shared/dto/household/request/household_create_request_dto.dart';

import 'test_helpers.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  late TestContext context;
  late HouseholdService householdService;

  setUp(() {
    context = createTestContext(defaultToken: 'jwt-household-token');
    householdService = HouseholdService(context.apiClient);
  });

  final mockHouseholdJson = {
    'id': 'house-123',
    'name': 'Baker Street Flat',
    'creationDate': '2026-01-01T12:00:00.000Z',
    'memberships': [
      {
        'user': {
          'id': 'u-1',
          'name': 'Sherlock',
          'surname': 'Holmes',
          'email': 'sherlock@holmes.co.uk',
        },
        'membership': {
          'isAdmin': true,
          'date': '2026-01-01T12:00:00.000Z',
        },
      }
    ],
  };

  group('HouseholdService - createHousehold', () {
    final createReq = HouseholdCreateRequestDTO(name: 'Baker Street Flat');

    test('creates household and returns HouseholdResponseDTO', () async {
      context.adapter.reply(200, mockHouseholdJson);

      final result = await householdService.createHousehold(createReq);

      final req = context.adapter.lastRequest!;
      expect(req.method, 'POST');
      expect(req.path, '/households');
      expect(req.data, createReq.toJson());

      expect(result.id, 'house-123');
      expect(result.name, 'Baker Street Flat');
      expect(result.memberships.length, 1);
      expect(result.memberships.first.user.name, 'Sherlock');
      expect(result.memberships.first.membership.isAdmin, isTrue);
    });

    test('throws ApiException on error', () async {
      context.adapter.reply(400, {'message': 'Household name too short'});

      expect(
        () => householdService.createHousehold(createReq),
        throwsA(isA<ApiException>().having((e) => e.statusCode, 'statusCode', 400)),
      );
    });
  });

  group('HouseholdService - getCurrentUserHousehold', () {
    test('fetches current household via GET /households/me', () async {
      context.adapter.reply(200, mockHouseholdJson);

      final result = await householdService.getCurrentUserHousehold();

      final req = context.adapter.lastRequest!;
      expect(req.method, 'GET');
      expect(req.path, '/households/me');

      expect(result.id, 'house-123');
      expect(result.name, 'Baker Street Flat');
    });

    test('throws ApiException on 404 Not Found (user not in a household)', () async {
      context.adapter.reply(404, {'message': 'User is not part of any household'});

      expect(
        () => householdService.getCurrentUserHousehold(),
        throwsA(isA<ApiException>().having((e) => e.statusCode, 'statusCode', 404)),
      );
    });
  });

  group('HouseholdService - addMember', () {
    final addReq = AddMemberRequestDTO(invitationCode: 'INVITE99');

    test('adds member via POST /households/members', () async {
      context.adapter.reply(200, mockHouseholdJson);

      final result = await householdService.addMember(addReq);

      final req = context.adapter.lastRequest!;
      expect(req.method, 'POST');
      expect(req.path, '/households/members');
      expect(req.data, addReq.toJson());

      expect(result.id, 'house-123');
    });

    test('throws ApiException on invalid invite code', () async {
      context.adapter.reply(400, {'message': 'Invalid or expired invitation code'});

      expect(
        () => householdService.addMember(addReq),
        throwsA(isA<ApiException>().having(
          (e) => e.message,
          'message',
          'Invalid or expired invitation code',
        )),
      );
    });
  });

  group('HouseholdService - getHouseholdMembers', () {
    final mockMembers = [
      {
        'user': {
          'id': 'u-1',
          'name': 'John',
          'surname': 'Watson',
          'email': 'watson@army.co.uk',
        },
        'membership': {
          'isAdmin': false,
          'date': '2026-02-01T10:00:00.000Z',
        },
      }
    ];

    test('fetches members via GET /households/members', () async {
      context.adapter.reply(200, mockMembers);

      final result = await householdService.getHouseholdMembers();

      final req = context.adapter.lastRequest!;
      expect(req.method, 'GET');
      expect(req.path, '/households/members');

      expect(result.length, 1);
      expect(result.first.user.name, 'John');
      expect(result.first.membership.isAdmin, isFalse);
    });

    test('throws ApiException on error', () async {
      context.adapter.reply(401, {'error': 'Unauthorized'});

      expect(
        () => householdService.getHouseholdMembers(),
        throwsA(isA<ApiException>().having((e) => e.statusCode, 'statusCode', 401)),
      );
    });
  });

  group('HouseholdService - removeMember', () {
    test('removes member via DELETE /households/members/:id', () async {
      context.adapter.reply(200, mockHouseholdJson);

      final result = await householdService.removeMember('u-2');

      final req = context.adapter.lastRequest!;
      expect(req.method, 'DELETE');
      expect(req.path, '/households/members/u-2');

      expect(result.id, 'house-123');
    });

    test('throws ApiException on 403 Forbidden', () async {
      context.adapter.reply(403, {'message': 'Only admins can remove members'});

      expect(
        () => householdService.removeMember('u-2'),
        throwsA(isA<ApiException>().having((e) => e.statusCode, 'statusCode', 403)),
      );
    });
  });

  group('HouseholdService - leaveHousehold', () {
    test('leaves household via DELETE /households/me', () async {
      context.adapter.reply(200, null);

      await householdService.leaveHousehold();

      final req = context.adapter.lastRequest!;
      expect(req.method, 'DELETE');
      expect(req.path, '/households/me');
    });

    test('throws ApiException on failure', () async {
      context.adapter.reply(400, {'message': 'Admins must transfer ownership before leaving'});

      expect(
        () => householdService.leaveHousehold(),
        throwsA(isA<ApiException>().having((e) => e.statusCode, 'statusCode', 400)),
      );
    });
  });

  group('HouseholdService - getInvitationCode', () {
    final mockInvite = {
      'invitationCode': 'CODE123',
      'refreshedAt': '2026-11-10T12:00:00.000Z',
    };

    test('gets invitation code via GET /households/invitation-code', () async {
      context.adapter.reply(200, mockInvite);

      final result = await householdService.getInvitationCode();

      final req = context.adapter.lastRequest!;
      expect(req.method, 'GET');
      expect(req.path, '/households/invitation-code');

      expect(result.invitationCode, 'CODE123');
      expect(result.refreshedAt, DateTime.parse('2026-11-10T12:00:00.000Z'));
    });

    test('throws ApiException on timeout', () async {
      context.adapter.replyError(DioExceptionType.connectionTimeout);

      expect(
        () => householdService.getInvitationCode(),
        throwsA(isA<ApiException>()),
      );
    });
  });

  group('HouseholdService - refreshInvitationCode', () {
    final mockRefreshed = {
      'invitationCode': 'NEWCODE999',
      'refreshedAt': '2026-11-10T13:00:00.000Z',
    };

    test('refreshes invitation code via POST /households/invitation-code/refresh', () async {
      context.adapter.reply(200, mockRefreshed);

      final result = await householdService.refreshInvitationCode();

      final req = context.adapter.lastRequest!;
      expect(req.method, 'POST');
      expect(req.path, '/households/invitation-code/refresh');

      expect(result.invitationCode, 'NEWCODE999');
      expect(result.refreshedAt, DateTime.parse('2026-11-10T13:00:00.000Z'));
    });

    test('throws ApiException on 403 Forbidden', () async {
      context.adapter.reply(403, {'message': 'Admin privilege required'});

      expect(
        () => householdService.refreshInvitationCode(),
        throwsA(isA<ApiException>().having((e) => e.statusCode, 'statusCode', 403)),
      );
    });
  });
}
