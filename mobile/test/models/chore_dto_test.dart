import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/shared/dto/chore/request/chore_assignment_create_request_dto.dart';
import 'package:mobile/shared/dto/chore/request/chore_assignment_filter_request_dto.dart';
import 'package:mobile/shared/dto/chore/request/chore_create_request_dto.dart';
import 'package:mobile/shared/dto/chore/request/chore_reassign_request_dto.dart';
import 'package:mobile/shared/dto/chore/request/chore_status_update_request_dto.dart';
import 'package:mobile/shared/dto/chore/response/assignment_overview_dto.dart';
import 'package:mobile/shared/dto/chore/response/chore_assignment_response_dto.dart';
import 'package:mobile/shared/dto/chore/response/chore_response_dto.dart';
import 'package:mobile/shared/dto/user/response/user_response_dto.dart';
import 'package:mobile/shared/enums/chore_status.dart';
import 'package:mobile/shared/utils/types/date_range.dart';

void main() {
  group('ChoreResponseDTO', () {
    test('should instantiate and serialize to JSON correctly', () {
      final dto = ChoreResponseDTO(
        id: 'chore-1',
        description: 'Take out trash',
        frequencyDays: 7,
      );

      expect(dto.id, 'chore-1');
      expect(dto.description, 'Take out trash');
      expect(dto.frequencyDays, 7);

      final json = dto.toJson();
      expect(json, {
        'id': 'chore-1',
        'description': 'Take out trash',
        'frequencyDays': 7,
      });

      final deserialized = ChoreResponseDTO.fromJson(json);
      expect(deserialized.id, dto.id);
      expect(deserialized.description, dto.description);
      expect(deserialized.frequencyDays, dto.frequencyDays);
    });
  });

  group('ChoreAssignmentResponseDTO', () {
    final user = UserResponseDTO(
      id: 'user-1',
      name: 'Alice',
      surname: 'Smith',
      email: 'alice@example.com',
    );

    test('should serialize and deserialize with ISO String date', () {
      final now = DateTime.parse('2026-11-10T12:00:00.000Z');
      final dto = ChoreAssignmentResponseDTO(
        assignmentId: 'asg-1',
        choreId: 'chore-1',
        choreDescription: 'Clean kitchen',
        assignedUser: user,
        dueDate: now,
        status: ChoreStatus.pending,
      );

      expect(dto.assignmentId, 'asg-1');
      expect(dto.choreDescription, 'Clean kitchen');
      expect(dto.status, ChoreStatus.pending);

      final json = dto.toJson();
      expect(json['status'], 'PENDING');
      expect(json['dueDate'], now.toIso8601String());

      final deserialized = ChoreAssignmentResponseDTO.fromJson(json);
      expect(deserialized.assignmentId, dto.assignmentId);
      expect(deserialized.dueDate, now);
      expect(deserialized.status, ChoreStatus.pending);
    });

    test('should deserialize with date as List format (years to seconds)', () {
      final json = {
        'assignmentId': 'asg-2',
        'choreId': 'chore-2',
        'choreDescription': 'Vacuum living room',
        'assignedUser': user.toJson(),
        'dueDate': [2026, 12, 25, 15, 30, 45],
        'status': 'OVERDUE',
      };

      final dto = ChoreAssignmentResponseDTO.fromJson(json);
      expect(dto.dueDate, DateTime(2026, 12, 25, 15, 30, 45));
      expect(dto.status, ChoreStatus.overdue);
    });

    test('should deserialize with date as short List format (3 items)', () {
      final json = {
        'assignmentId': 'asg-3',
        'choreId': 'chore-3',
        'choreDescription': 'Water plants',
        'assignedUser': user.toJson(),
        'dueDate': [2026, 5, 1],
        'status': 'COMPLETED',
      };

      final dto = ChoreAssignmentResponseDTO.fromJson(json);
      expect(dto.dueDate, DateTime(2026, 5, 1));
      expect(dto.status, ChoreStatus.completed);
    });

    test('should deserialize with null or invalid date', () {
      final jsonNull = {
        'assignmentId': 'asg-4',
        'choreId': 'chore-4',
        'choreDescription': 'Mow lawn',
        'assignedUser': user.toJson(),
        'dueDate': null,
        'status': 'PENDING',
      };
      final dtoNull = ChoreAssignmentResponseDTO.fromJson(jsonNull);
      expect(dtoNull.dueDate, isNull);

      final jsonInvalid = {
        'assignmentId': 'asg-5',
        'choreId': 'chore-5',
        'choreDescription': 'Fix door',
        'assignedUser': user.toJson(),
        'dueDate': 12345,
        'status': 'PENDING',
      };
      final dtoInvalid = ChoreAssignmentResponseDTO.fromJson(jsonInvalid);
      expect(dtoInvalid.dueDate, isNull);
    });
  });

  group('AssignmentOverviewDTO', () {
    test('should instantiate and serialize with non-null values', () {
      final dto = AssignmentOverviewDTO(
        pendingAssignments: 3,
        overdueAssignments: 1,
      );

      final json = dto.toJson();
      expect(json, {
        'pendingAssignments': 3,
        'overdueAssignments': 1,
      });

      final deserialized = AssignmentOverviewDTO.fromJson(json);
      expect(deserialized.pendingAssignments, 3);
      expect(deserialized.overdueAssignments, 1);
    });

    test('should instantiate and serialize with null values', () {
      final dto = AssignmentOverviewDTO();
      expect(dto.pendingAssignments, isNull);
      expect(dto.overdueAssignments, isNull);

      final json = dto.toJson();
      expect(json['pendingAssignments'], isNull);
      expect(json['overdueAssignments'], isNull);

      final deserialized = AssignmentOverviewDTO.fromJson({});
      expect(deserialized.pendingAssignments, isNull);
      expect(deserialized.overdueAssignments, isNull);
    });
  });

  group('ChoreCreateRequestDTO', () {
    test('should serialize and deserialize correctly', () {
      final dto = ChoreCreateRequestDTO(
        description: 'Wash dishes',
        frequencyDays: 1,
        householdId: 'house-123',
      );

      expect(dto.description, 'Wash dishes');
      expect(dto.frequencyDays, 1);
      expect(dto.householdId, 'house-123');

      final json = dto.toJson();
      expect(json, {
        'description': 'Wash dishes',
        'frequencyDays': 1,
        'householdId': 'house-123',
      });

      final deserialized = ChoreCreateRequestDTO.fromJson(json);
      expect(deserialized.description, dto.description);
      expect(deserialized.frequencyDays, dto.frequencyDays);
      expect(deserialized.householdId, dto.householdId);
    });
  });

  group('ChoreAssignmentCreateRequestDTO', () {
    test('should serialize and deserialize with dueDate present', () {
      final due = DateTime.parse('2026-12-01T10:00:00.000Z');
      final dto = ChoreAssignmentCreateRequestDTO(
        choreId: 'chore-1',
        assignedUserId: 'user-1',
        dueDate: due,
      );

      final json = dto.toJson();
      expect(json, {
        'choreId': 'chore-1',
        'assignedUserId': 'user-1',
        'dueDate': due.toIso8601String(),
      });

      final deserialized = ChoreAssignmentCreateRequestDTO.fromJson(json);
      expect(deserialized.choreId, 'chore-1');
      expect(deserialized.assignedUserId, 'user-1');
      expect(deserialized.dueDate, due);
    });

    test('should serialize and deserialize with dueDate null', () {
      final dto = ChoreAssignmentCreateRequestDTO(
        choreId: 'chore-2',
        assignedUserId: 'user-2',
        dueDate: null,
      );

      final json = dto.toJson();
      expect(json['dueDate'], isNull);

      final deserialized = ChoreAssignmentCreateRequestDTO.fromJson(json);
      expect(deserialized.dueDate, isNull);
    });
  });

  group('ChoreAssignmentFilterRequestDTO', () {
    test('should serialize and deserialize with all fields populated', () {
      final dateRange = DateRange(
        startDate: DateTime.parse('2026-01-01T00:00:00.000Z'),
        endDate: DateTime.parse('2026-01-31T23:59:59.000Z'),
      );
      final dto = ChoreAssignmentFilterRequestDTO(
        statuses: [ChoreStatus.pending, ChoreStatus.overdue],
        assigneeId: 'user-99',
        descriptionContains: 'clean',
        dateRange: dateRange,
      );

      final json = dto.toJson();
      expect(json['statuses'], ['PENDING', 'OVERDUE']);
      expect(json['assigneeId'], 'user-99');
      expect(json['descriptionContains'], 'clean');
      expect(json['dateRange'], dateRange.toJson());

      final deserialized = ChoreAssignmentFilterRequestDTO.fromJson(json);
      expect(deserialized.statuses, [ChoreStatus.pending, ChoreStatus.overdue]);
      expect(deserialized.assigneeId, 'user-99');
      expect(deserialized.descriptionContains, 'clean');
      expect(deserialized.dateRange.startDate, dateRange.startDate);
      expect(deserialized.dateRange.endDate, dateRange.endDate);
    });

    test('should serialize and deserialize with optional fields null', () {
      final dateRange = DateRange();
      final dto = ChoreAssignmentFilterRequestDTO(
        dateRange: dateRange,
      );

      final json = dto.toJson();
      expect(json['statuses'], isNull);
      expect(json['assigneeId'], isNull);
      expect(json['descriptionContains'], isNull);

      final deserialized = ChoreAssignmentFilterRequestDTO.fromJson(json);
      expect(deserialized.statuses, isNull);
      expect(deserialized.assigneeId, isNull);
      expect(deserialized.descriptionContains, isNull);
    });
  });

  group('ChoreStatusUpdateRequestDTO', () {
    test('should serialize and deserialize correctly', () {
      for (final status in ChoreStatus.values) {
        final dto = ChoreStatusUpdateRequestDTO(newStatus: status);
        final json = dto.toJson();
        final deserialized = ChoreStatusUpdateRequestDTO.fromJson(json);
        expect(deserialized.newStatus, status);
      }
    });
  });

  group('ChoreReassignRequestDTO', () {
    test('should serialize and deserialize correctly', () {
      final dto = ChoreReassignRequestDTO(newAssigneeId: 'user-new-42');
      expect(dto.newAssigneeId, 'user-new-42');

      final json = dto.toJson();
      expect(json, {'newAssigneeId': 'user-new-42'});

      final deserialized = ChoreReassignRequestDTO.fromJson(json);
      expect(deserialized.newAssigneeId, 'user-new-42');
    });
  });
}
