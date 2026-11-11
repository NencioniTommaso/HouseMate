import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/core/network/api_exception.dart';
import 'package:mobile/services/chore_service.dart';
import 'package:mobile/shared/dto/chore/request/chore_assignment_create_request_dto.dart';
import 'package:mobile/shared/dto/chore/request/chore_assignment_filter_request_dto.dart';
import 'package:mobile/shared/dto/chore/request/chore_create_request_dto.dart';
import 'package:mobile/shared/dto/chore/request/chore_reassign_request_dto.dart';
import 'package:mobile/shared/dto/chore/request/chore_status_update_request_dto.dart';
import 'package:mobile/shared/dto/chore/response/assignment_overview_dto.dart';
import 'package:mobile/shared/dto/chore/response/chore_assignment_response_dto.dart';
import 'package:mobile/shared/dto/chore/response/chore_response_dto.dart';
import 'package:mobile/shared/enums/chore_status.dart';
import 'package:mobile/shared/utils/types/date_range.dart';

import 'test_helpers.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  late TestContext context;
  late ChoreService choreService;

  setUp(() {
    context = createTestContext(defaultToken: 'jwt-chore-token');
    choreService = ChoreService(context.apiClient);
  });

  group('ChoreService - createChore', () {
    final createReq = ChoreCreateRequestDTO(
      description: 'Clean bathroom',
      frequencyDays: 7,
      householdId: 'house-1',
    );

    final mockResponseJson = {
      'id': 'chore-101',
      'description': 'Clean bathroom',
      'frequencyDays': 7,
    };

    test('creates chore successfully and returns ChoreResponseDTO', () async {
      context.adapter.reply(200, mockResponseJson);

      final result = await choreService.createChore(createReq);

      final req = context.adapter.lastRequest!;
      expect(req.method, 'POST');
      expect(req.path, '/chores');
      expect(req.data, createReq.toJson());
      expect(req.headers['Authorization'], 'Bearer jwt-chore-token');

      expect(result.id, 'chore-101');
      expect(result.description, 'Clean bathroom');
      expect(result.frequencyDays, 7);
    });

    test('throws ApiException on failure', () async {
      context.adapter.reply(400, {'message': 'Invalid frequency'});

      expect(
        () => choreService.createChore(createReq),
        throwsA(isA<ApiException>().having((e) => e.statusCode, 'statusCode', 400)),
      );
    });
  });

  group('ChoreService - deleteChore', () {
    test('deletes chore successfully', () async {
      context.adapter.reply(200, null);

      await choreService.deleteChore('chore-101');

      final req = context.adapter.lastRequest!;
      expect(req.method, 'DELETE');
      expect(req.path, '/chores/chore-101');
    });

    test('throws ApiException on 404', () async {
      context.adapter.reply(404, {'message': 'Chore not found'});

      expect(
        () => choreService.deleteChore('chore-999'),
        throwsA(isA<ApiException>().having((e) => e.statusCode, 'statusCode', 404)),
      );
    });
  });

  group('ChoreService - createAssignment', () {
    final due = DateTime.parse('2026-11-20T10:00:00.000Z');
    final assignReq = ChoreAssignmentCreateRequestDTO(
      choreId: 'chore-101',
      assignedUserId: 'user-1',
      dueDate: due,
    );

    final mockAssignmentJson = {
      'assignmentId': 'asg-501',
      'choreId': 'chore-101',
      'choreDescription': 'Clean bathroom',
      'assignedUser': {
        'id': 'user-1',
        'name': 'Bob',
        'surname': 'Jones',
        'email': 'bob@example.com',
      },
      'dueDate': due.toIso8601String(),
      'status': 'PENDING',
    };

    test('creates assignment and returns ChoreAssignmentResponseDTO', () async {
      context.adapter.reply(200, mockAssignmentJson);

      final result = await choreService.createAssignment(assignReq);

      final req = context.adapter.lastRequest!;
      expect(req.method, 'POST');
      expect(req.path, '/chores/assignments');
      expect(req.data, assignReq.toJson());

      expect(result.assignmentId, 'asg-501');
      expect(result.choreDescription, 'Clean bathroom');
      expect(result.status, ChoreStatus.pending);
      expect(result.assignedUser.name, 'Bob');
    });

    test('throws ApiException on error', () async {
      context.adapter.reply(403, {'message': 'Not allowed'});

      expect(
        () => choreService.createAssignment(assignReq),
        throwsA(isA<ApiException>().having((e) => e.statusCode, 'statusCode', 403)),
      );
    });
  });

  group('ChoreService - deleteChoreAssignment', () {
    test('deletes chore assignment successfully', () async {
      context.adapter.reply(200, null);

      await choreService.deleteChoreAssignment('asg-501');

      final req = context.adapter.lastRequest!;
      expect(req.method, 'DELETE');
      expect(req.path, '/chores/assignments/asg-501');
    });

    test('throws ApiException on failure', () async {
      context.adapter.replyError(DioExceptionType.connectionTimeout);

      expect(
        () => choreService.deleteChoreAssignment('asg-501'),
        throwsA(isA<ApiException>()),
      );
    });
  });

  group('ChoreService - updateChoreAssignmentStatus', () {
    final statusReq = ChoreStatusUpdateRequestDTO(newStatus: ChoreStatus.completed);

    test('updates chore assignment status via PATCH', () async {
      context.adapter.reply(200, null);

      await choreService.updateChoreAssignmentStatus('asg-501', statusReq);

      final req = context.adapter.lastRequest!;
      expect(req.method, 'PATCH');
      expect(req.path, '/chores/assignments/asg-501/status');
      expect(req.data, statusReq.toJson());
    });

    test('throws ApiException on bad request', () async {
      context.adapter.reply(400, {'message': 'Invalid status transition'});

      expect(
        () => choreService.updateChoreAssignmentStatus('asg-501', statusReq),
        throwsA(isA<ApiException>().having((e) => e.statusCode, 'statusCode', 400)),
      );
    });
  });

  group('ChoreService - reassignChore', () {
    final reassignReq = ChoreReassignRequestDTO(newAssigneeId: 'user-2');

    final mockReassignedJson = {
      'assignmentId': 'asg-501',
      'choreId': 'chore-101',
      'choreDescription': 'Clean bathroom',
      'assignedUser': {
        'id': 'user-2',
        'name': 'Alice',
        'surname': 'Smith',
        'email': 'alice@example.com',
      },
      'dueDate': '2026-11-20T10:00:00.000Z',
      'status': 'PENDING',
    };

    test('reassigns chore via PATCH and returns ChoreAssignmentResponseDTO', () async {
      context.adapter.reply(200, mockReassignedJson);

      final result = await choreService.reassignChore('asg-501', reassignReq);

      final req = context.adapter.lastRequest!;
      expect(req.method, 'PATCH');
      expect(req.path, '/chores/assignments/asg-501/reassign');
      expect(req.data, reassignReq.toJson());

      expect(result.assignedUser.id, 'user-2');
      expect(result.assignedUser.name, 'Alice');
    });

    test('throws ApiException on failure', () async {
      context.adapter.reply(404, {'message': 'Assignee not found'});

      expect(
        () => choreService.reassignChore('asg-501', reassignReq),
        throwsA(isA<ApiException>()),
      );
    });
  });

  group('ChoreService - getFilteredChoreAssignments', () {
    final filterReq = ChoreAssignmentFilterRequestDTO(
      statuses: [ChoreStatus.pending, ChoreStatus.overdue],
      assigneeId: 'user-1',
      descriptionContains: 'clean',
      dateRange: DateRange(
        startDate: DateTime.parse('2026-11-01T00:00:00.000Z'),
        endDate: DateTime.parse('2026-11-30T23:59:59.000Z'),
      ),
    );

    final mockListJson = [
      {
        'assignmentId': 'asg-1',
        'choreId': 'chore-1',
        'choreDescription': 'Clean kitchen',
        'assignedUser': {
          'id': 'user-1',
          'name': 'Bob',
          'surname': 'Jones',
          'email': 'bob@example.com',
        },
        'dueDate': '2026-11-15T12:00:00.000Z',
        'status': 'PENDING',
      }
    ];

    test('passes all query parameters and returns list of assignments', () async {
      context.adapter.reply(200, mockListJson);

      final result = await choreService.getFilteredChoreAssignments(filterReq);

      final req = context.adapter.lastRequest!;
      expect(req.method, 'GET');
      expect(req.path, '/chores/assignments');
      expect(req.queryParameters['statuses'], ['PENDING', 'OVERDUE']);
      expect(req.queryParameters['assigneeId'], 'user-1');
      expect(req.queryParameters['descriptionContains'], 'clean');
      expect(
        req.queryParameters['dateRange.startDate'],
        DateTime.parse('2026-11-01T00:00:00.000Z').toIso8601String(),
      );
      expect(
        req.queryParameters['dateRange.endDate'],
        DateTime.parse('2026-11-30T23:59:59.000Z').toIso8601String(),
      );

      expect(result.length, 1);
      expect(result.first.assignmentId, 'asg-1');
    });

    test('handles empty filter without query parameters', () async {
      context.adapter.reply(200, <dynamic>[]);

      final emptyFilter = ChoreAssignmentFilterRequestDTO(dateRange: DateRange());
      final result = await choreService.getFilteredChoreAssignments(emptyFilter);

      final req = context.adapter.lastRequest!;
      expect(req.queryParameters.isEmpty, isTrue);
      expect(result, isEmpty);
    });

    test('throws ApiException on server error', () async {
      context.adapter.reply(500, {'message': 'Database failure'});

      expect(
        () => choreService.getFilteredChoreAssignments(filterReq),
        throwsA(isA<ApiException>().having((e) => e.statusCode, 'statusCode', 500)),
      );
    });
  });

  group('ChoreService - getAllHouseholdChores', () {
    test('returns list of ChoreResponseDTO', () async {
      final mockData = [
        {'id': 'c-1', 'description': 'Dishes', 'frequencyDays': 1},
        {'id': 'c-2', 'description': 'Laundry', 'frequencyDays': 3},
      ];
      context.adapter.reply(200, mockData);

      final result = await choreService.getAllHouseholdChores();

      final req = context.adapter.lastRequest!;
      expect(req.method, 'GET');
      expect(req.path, '/chores');

      expect(result.length, 2);
      expect(result[0].id, 'c-1');
      expect(result[1].id, 'c-2');
    });

    test('throws ApiException on error', () async {
      context.adapter.reply(401, {'error': 'Unauthorized'});

      expect(
        () => choreService.getAllHouseholdChores(),
        throwsA(isA<ApiException>().having((e) => e.statusCode, 'statusCode', 401)),
      );
    });
  });

  group('ChoreService - getHouseholdAssignmentOverview', () {
    test('returns AssignmentOverviewDTO', () async {
      final mockData = {'pendingAssignments': 5, 'overdueAssignments': 2};
      context.adapter.reply(200, mockData);

      final result = await choreService.getHouseholdAssignmentOverview();

      final req = context.adapter.lastRequest!;
      expect(req.method, 'GET');
      expect(req.path, '/chores/assignments/overview');

      expect(result.pendingAssignments, 5);
      expect(result.overdueAssignments, 2);
    });

    test('throws ApiException on error', () async {
      context.adapter.reply(403, {'message': 'Forbidden'});

      expect(
        () => choreService.getHouseholdAssignmentOverview(),
        throwsA(isA<ApiException>().having((e) => e.statusCode, 'statusCode', 403)),
      );
    });
  });

  group('ChoreService - getUserAssignmentOverview', () {
    test('returns AssignmentOverviewDTO for me', () async {
      final mockData = {'pendingAssignments': 1, 'overdueAssignments': 0};
      context.adapter.reply(200, mockData);

      final result = await choreService.getUserAssignmentOverview();

      final req = context.adapter.lastRequest!;
      expect(req.method, 'GET');
      expect(req.path, '/chores/assignments/me');

      expect(result.pendingAssignments, 1);
      expect(result.overdueAssignments, 0);
    });

    test('throws ApiException on error', () async {
      context.adapter.reply(500, {'message': 'Server error'});

      expect(
        () => choreService.getUserAssignmentOverview(),
        throwsA(isA<ApiException>().having((e) => e.statusCode, 'statusCode', 500)),
      );
    });
  });
}
