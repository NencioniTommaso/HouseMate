import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/core/network/api_exception.dart';
import 'package:mobile/services/chore_service.dart';
import 'package:mobile/state/chore_provider.dart';
import 'package:mobile/shared/dto/chore/request/chore_assignment_create_request_dto.dart';
import 'package:mobile/shared/dto/chore/request/chore_assignment_filter_request_dto.dart';
import 'package:mobile/shared/dto/chore/request/chore_create_request_dto.dart';
import 'package:mobile/shared/dto/chore/request/chore_status_update_request_dto.dart';
import 'package:mobile/shared/dto/chore/response/assignment_overview_dto.dart';
import 'package:mobile/shared/dto/chore/response/chore_assignment_response_dto.dart';
import 'package:mobile/shared/dto/chore/response/chore_response_dto.dart';
import 'package:mobile/shared/dto/user/response/user_response_dto.dart';
import 'package:mobile/shared/enums/chore_status.dart';
import 'package:mobile/shared/utils/types/date_range.dart';

import '../services/test_helpers.dart';
import 'state_test_helpers.dart';

class FakeChoreService extends ChoreService {
  FakeChoreService(super.apiClient);

  AssignmentOverviewDTO mockOverview = AssignmentOverviewDTO(
    pendingAssignments: 3,
    overdueAssignments: 1,
  );

  List<ChoreAssignmentResponseDTO> mockAssignments = [];
  List<ChoreResponseDTO> mockHouseholdChores = [];

  ChoreAssignmentFilterRequestDTO? lastFilterRequest;
  ChoreCreateRequestDTO? lastCreateChoreRequest;
  ChoreAssignmentCreateRequestDTO? lastCreateAssignmentRequest;
  String? lastUpdatedAssignmentId;
  ChoreStatusUpdateRequestDTO? lastUpdatedStatusRequest;
  String? lastDeletedChoreId;
  String? lastDeletedAssignmentId;

  Exception? errorToThrow;

  @override
  Future<AssignmentOverviewDTO> getUserAssignmentOverview() async {
    if (errorToThrow != null) throw errorToThrow!;
    return mockOverview;
  }

  @override
  Future<List<ChoreAssignmentResponseDTO>> getFilteredChoreAssignments(
    ChoreAssignmentFilterRequestDTO requestDTO,
  ) async {
    if (errorToThrow != null) throw errorToThrow!;
    lastFilterRequest = requestDTO;
    return mockAssignments;
  }

  @override
  Future<List<ChoreResponseDTO>> getAllHouseholdChores() async {
    if (errorToThrow != null) throw errorToThrow!;
    return mockHouseholdChores;
  }

  @override
  Future<void> updateChoreAssignmentStatus(
    String assignmentId,
    ChoreStatusUpdateRequestDTO requestDTO,
  ) async {
    if (errorToThrow != null) throw errorToThrow!;
    lastUpdatedAssignmentId = assignmentId;
    lastUpdatedStatusRequest = requestDTO;
  }

  @override
  Future<ChoreResponseDTO> createChore(ChoreCreateRequestDTO requestDTO) async {
    if (errorToThrow != null) throw errorToThrow!;
    lastCreateChoreRequest = requestDTO;
    return ChoreResponseDTO(
      id: 'chore-123',
      description: requestDTO.description,
      frequencyDays: requestDTO.frequencyDays,
    );
  }

  @override
  Future<void> deleteChore(String choreId) async {
    if (errorToThrow != null) throw errorToThrow!;
    lastDeletedChoreId = choreId;
  }

  @override
  Future<void> deleteChoreAssignment(String assignmentId) async {
    if (errorToThrow != null) throw errorToThrow!;
    lastDeletedAssignmentId = assignmentId;
  }

  @override
  Future<ChoreAssignmentResponseDTO> createAssignment(
    ChoreAssignmentCreateRequestDTO requestDTO,
  ) async {
    if (errorToThrow != null) throw errorToThrow!;
    lastCreateAssignmentRequest = requestDTO;
    return ChoreAssignmentResponseDTO(
      assignmentId: 'assign-456',
      choreId: requestDTO.choreId,
      choreDescription: 'Generated Chore',
      assignedUser: UserResponseDTO(
        id: requestDTO.assignedUserId,
        name: 'User',
        surname: 'Test',
        email: 'user@test.com',
      ),
      dueDate: requestDTO.dueDate,
      status: ChoreStatus.pending,
    );
  }
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  late TestContext context;
  late FakeUiService uiService;
  late FakeChoreService fakeChoreService;
  late ChoreProvider choreProvider;

  final sampleChore = ChoreResponseDTO(
    id: 'c1',
    description: 'Clean kitchen',
    frequencyDays: 7,
  );

  final sampleAssignment = ChoreAssignmentResponseDTO(
    assignmentId: 'a1',
    choreId: 'c1',
    choreDescription: 'Clean kitchen',
    assignedUser: UserResponseDTO(
      id: 'u1',
      name: 'Alice',
      surname: 'Smith',
      email: 'alice@example.com',
    ),
    dueDate: DateTime.now().add(const Duration(days: 2)),
    status: ChoreStatus.pending,
  );

  setUp(() {
    context = createTestContext();
    uiService = FakeUiService();
    fakeChoreService = FakeChoreService(context.apiClient);
    fakeChoreService.mockHouseholdChores = [sampleChore];
    fakeChoreService.mockAssignments = [sampleAssignment];

    choreProvider = ChoreProvider(
      apiClient: context.apiClient,
      uiService: uiService,
      choreService: fakeChoreService,
    );
  });

  group('ChoreProvider - Initial State', () {
    test('initial values are properly reset and empty', () {
      expect(choreProvider.overview, isNull);
      expect(choreProvider.assignments, isEmpty);
      expect(choreProvider.householdChores, isEmpty);
      expect(choreProvider.isLoading, false);
      expect(choreProvider.errorMessage, isNull);
    });
  });

  group('ChoreProvider - loadAssignments', () {
    test('successfully loads overview, assignments, and household chores', () async {
      final loadingStates = <bool>[];
      choreProvider.addListener(() {
        loadingStates.add(choreProvider.isLoading);
      });

      await choreProvider.loadAssignments();

      expect(choreProvider.overview, isNotNull);
      expect(choreProvider.overview!.pendingAssignments, 3);
      expect(choreProvider.overview!.overdueAssignments, 1);
      expect(choreProvider.assignments.length, 1);
      expect(choreProvider.assignments.first.assignmentId, 'a1');
      expect(choreProvider.householdChores.length, 1);
      expect(choreProvider.householdChores.first.description, 'Clean kitchen');
      expect(choreProvider.isLoading, false);
      expect(choreProvider.errorMessage, isNull);
      expect(loadingStates, contains(true));
      // Satisfying backend's @NotNull dateRange default
      expect(fakeChoreService.lastFilterRequest?.dateRange, isNotNull);
    });

    test('passes explicit filter arguments and dateRange to service', () async {
      final customRange = DateRange(
        startDate: DateTime(2026, 1, 1),
        endDate: DateTime(2026, 1, 31),
      );

      await choreProvider.loadAssignments(
        statuses: [ChoreStatus.pending],
        assigneeId: 'u1',
        descriptionContains: 'kitchen',
        dateRange: customRange,
      );

      final filterReq = fakeChoreService.lastFilterRequest;
      expect(filterReq, isNotNull);
      expect(filterReq!.statuses, [ChoreStatus.pending]);
      expect(filterReq.assigneeId, 'u1');
      expect(filterReq.descriptionContains, 'kitchen');
      expect(filterReq.dateRange, customRange);
    });

    test('handles ApiException correctly', () async {
      fakeChoreService.errorToThrow = ApiException(
        message: 'Network timeout',
        statusCode: 408,
      );

      await choreProvider.loadAssignments();

      expect(choreProvider.isLoading, false);
      expect(choreProvider.errorMessage, 'Network timeout');
      expect(uiService.errorMessages, contains('Network timeout'));
    });

    test('handles unexpected generic error correctly', () async {
      fakeChoreService.errorToThrow = Exception('Database unreachable');

      await choreProvider.loadAssignments();

      expect(choreProvider.isLoading, false);
      expect(
        choreProvider.errorMessage,
        'An unexpected error occurred while fetching chore assignments.',
      );
      expect(
        uiService.errorMessages,
        contains('An unexpected error occurred while fetching chore assignments.'),
      );
    });
  });

  group('ChoreProvider - updateAssignmentStatus', () {
    test('successful update notifies UI, reloads assignments, and returns true', () async {
      final success = await choreProvider.updateAssignmentStatus('a1', ChoreStatus.completed);

      expect(success, true);
      expect(fakeChoreService.lastUpdatedAssignmentId, 'a1');
      expect(fakeChoreService.lastUpdatedStatusRequest?.newStatus, ChoreStatus.completed);
      expect(uiService.successMessages, contains('Status updated successfully'));
      expect(choreProvider.assignments.length, 1);
      expect(choreProvider.isLoading, false);
    });

    test('handles ApiException on updateAssignmentStatus', () async {
      fakeChoreService.errorToThrow = ApiException(message: 'Assignment not found');

      final success = await choreProvider.updateAssignmentStatus('a1', ChoreStatus.completed);

      expect(success, false);
      expect(choreProvider.errorMessage, 'Assignment not found');
      expect(uiService.errorMessages, contains('Assignment not found'));
      expect(choreProvider.isLoading, false);
    });

    test('handles generic error on updateAssignmentStatus', () async {
      fakeChoreService.errorToThrow = Exception('Internal server glitch');

      final success = await choreProvider.updateAssignmentStatus('a1', ChoreStatus.completed);

      expect(success, false);
      expect(
        choreProvider.errorMessage,
        'An unexpected error occurred while updating status.',
      );
      expect(
        uiService.errorMessages,
        contains('An unexpected error occurred while updating status.'),
      );
    });
  });

  group('ChoreProvider - loadHouseholdChores', () {
    test('loads household chores successfully', () async {
      await choreProvider.loadHouseholdChores();

      expect(choreProvider.householdChores.length, 1);
      expect(choreProvider.householdChores.first.id, 'c1');
      expect(choreProvider.isLoading, false);
      expect(choreProvider.errorMessage, isNull);
    });

    test('handles ApiException on loadHouseholdChores', () async {
      fakeChoreService.errorToThrow = ApiException(message: 'Household not found');

      await choreProvider.loadHouseholdChores();

      expect(choreProvider.errorMessage, 'Household not found');
      expect(uiService.errorMessages, contains('Household not found'));
    });

    test('handles generic error on loadHouseholdChores', () async {
      fakeChoreService.errorToThrow = Exception('Fetch failed');

      await choreProvider.loadHouseholdChores();

      expect(
        choreProvider.errorMessage,
        'An unexpected error occurred while fetching chores.',
      );
      expect(
        uiService.errorMessages,
        contains('An unexpected error occurred while fetching chores.'),
      );
    });
  });

  group('ChoreProvider - createChore', () {
    test('creates chore successfully and refreshes list', () async {
      final success = await choreProvider.createChore('Vacuum living room', 3, 'h1');

      expect(success, true);
      expect(fakeChoreService.lastCreateChoreRequest?.description, 'Vacuum living room');
      expect(fakeChoreService.lastCreateChoreRequest?.frequencyDays, 3);
      expect(fakeChoreService.lastCreateChoreRequest?.householdId, 'h1');
      expect(uiService.successMessages, contains('Chore created successfully'));
      expect(choreProvider.isLoading, false);
    });

    test('handles ApiException on createChore', () async {
      fakeChoreService.errorToThrow = ApiException(message: 'Invalid chore data');

      final success = await choreProvider.createChore('Bad Chore', 0, 'h1');

      expect(success, false);
      expect(choreProvider.errorMessage, 'Invalid chore data');
      expect(uiService.errorMessages, contains('Invalid chore data'));
    });

    test('handles generic error on createChore', () async {
      fakeChoreService.errorToThrow = Exception('Crash');

      final success = await choreProvider.createChore('Bad Chore', 0, 'h1');

      expect(success, false);
      expect(
        choreProvider.errorMessage,
        'An unexpected error occurred while creating chore.',
      );
    });
  });

  group('ChoreProvider - deleteChore', () {
    test('deletes chore successfully and refreshes list', () async {
      final success = await choreProvider.deleteChore('c1');

      expect(success, true);
      expect(fakeChoreService.lastDeletedChoreId, 'c1');
      expect(uiService.successMessages, contains('Chore deleted successfully'));
    });

    test('handles ApiException on deleteChore', () async {
      fakeChoreService.errorToThrow = ApiException(message: 'Chore in use');

      final success = await choreProvider.deleteChore('c1');

      expect(success, false);
      expect(choreProvider.errorMessage, 'Chore in use');
      expect(uiService.errorMessages, contains('Chore in use'));
    });

    test('handles generic error on deleteChore', () async {
      fakeChoreService.errorToThrow = Exception('Cannot delete');

      final success = await choreProvider.deleteChore('c1');

      expect(success, false);
      expect(
        choreProvider.errorMessage,
        'An unexpected error occurred while deleting chore.',
      );
    });
  });

  group('ChoreProvider - deleteAssignment', () {
    test('deletes assignment successfully and refreshes assignments', () async {
      final success = await choreProvider.deleteAssignment('a1');

      expect(success, true);
      expect(fakeChoreService.lastDeletedAssignmentId, 'a1');
      expect(uiService.successMessages, contains('Assignment deleted successfully'));
    });

    test('handles ApiException on deleteAssignment', () async {
      fakeChoreService.errorToThrow = ApiException(message: 'Assignment not found');

      final success = await choreProvider.deleteAssignment('a1');

      expect(success, false);
      expect(choreProvider.errorMessage, 'Assignment not found');
      expect(uiService.errorMessages, contains('Assignment not found'));
    });

    test('handles generic error on deleteAssignment', () async {
      fakeChoreService.errorToThrow = Exception('Delete failed');

      final success = await choreProvider.deleteAssignment('a1');

      expect(success, false);
      expect(
        choreProvider.errorMessage,
        'An unexpected error occurred while deleting assignment.',
      );
    });
  });

  group('ChoreProvider - createAssignment', () {
    test('creates assignment successfully and refreshes list', () async {
      final due = DateTime.now().add(const Duration(days: 5));
      final success = await choreProvider.createAssignment('c1', 'u1', due);

      expect(success, true);
      expect(fakeChoreService.lastCreateAssignmentRequest?.choreId, 'c1');
      expect(fakeChoreService.lastCreateAssignmentRequest?.assignedUserId, 'u1');
      expect(fakeChoreService.lastCreateAssignmentRequest?.dueDate, due);
      expect(uiService.successMessages, contains('Assignment created successfully'));
    });

    test('handles ApiException on createAssignment', () async {
      fakeChoreService.errorToThrow = ApiException(message: 'User does not belong to household');

      final success = await choreProvider.createAssignment('c1', 'u99', DateTime.now());

      expect(success, false);
      expect(choreProvider.errorMessage, 'User does not belong to household');
      expect(uiService.errorMessages, contains('User does not belong to household'));
    });

    test('handles generic error on createAssignment', () async {
      fakeChoreService.errorToThrow = Exception('Network down');

      final success = await choreProvider.createAssignment('c1', 'u1', DateTime.now());

      expect(success, false);
      expect(
        choreProvider.errorMessage,
        'An unexpected error occurred while creating assignment.',
      );
    });
  });

  group('ChoreProvider - createChoreAndAssignment', () {
    test('creates both chore and assignment sequentially and refreshes', () async {
      final due = DateTime.now().add(const Duration(days: 4));
      final success = await choreProvider.createChoreAndAssignment(
        'Mow the lawn',
        'u1',
        due,
        'h1',
      );

      expect(success, true);
      expect(fakeChoreService.lastCreateChoreRequest?.description, 'Mow the lawn');
      expect(fakeChoreService.lastCreateChoreRequest?.frequencyDays, 0);
      expect(fakeChoreService.lastCreateChoreRequest?.householdId, 'h1');
      expect(fakeChoreService.lastCreateAssignmentRequest?.choreId, 'chore-123');
      expect(fakeChoreService.lastCreateAssignmentRequest?.assignedUserId, 'u1');
      expect(fakeChoreService.lastCreateAssignmentRequest?.dueDate, due);
      expect(
        uiService.successMessages,
        contains('Chore and assignment created successfully'),
      );
    });

    test('handles ApiException on createChoreAndAssignment', () async {
      fakeChoreService.errorToThrow = ApiException(message: 'Household not active');

      final success = await choreProvider.createChoreAndAssignment('Chore', 'u1', null, 'h1');

      expect(success, false);
      expect(choreProvider.errorMessage, 'Household not active');
      expect(uiService.errorMessages, contains('Household not active'));
    });

    test('handles generic error on createChoreAndAssignment', () async {
      fakeChoreService.errorToThrow = Exception('Service error');

      final success = await choreProvider.createChoreAndAssignment('Chore', 'u1', null, 'h1');

      expect(success, false);
      expect(
        choreProvider.errorMessage,
        'An unexpected error occurred while creating assignment.',
      );
    });
  });

  group('ChoreProvider - clear', () {
    test('resets all state variables and notifies listeners', () async {
      await choreProvider.loadAssignments();
      expect(choreProvider.overview, isNotNull);
      expect(choreProvider.assignments, isNotEmpty);
      expect(choreProvider.householdChores, isNotEmpty);

      var notified = false;
      choreProvider.addListener(() {
        notified = true;
      });

      choreProvider.clear();

      expect(choreProvider.overview, isNull);
      expect(choreProvider.assignments, isEmpty);
      expect(choreProvider.householdChores, isEmpty);
      expect(choreProvider.isLoading, false);
      expect(choreProvider.errorMessage, isNull);
      expect(notified, true);
    });
  });
}
