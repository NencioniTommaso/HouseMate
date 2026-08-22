import 'package:flutter/material.dart';
import '../core/network/api_client.dart';
import '../core/network/api_exception.dart';
import '../services/chore_service.dart';
import '../shared/dto/chore/response/assignment_overview_dto.dart';
import '../shared/dto/chore/response/chore_assignment_response_dto.dart';
import '../shared/dto/chore/response/chore_response_dto.dart';
import '../shared/dto/chore/request/chore_assignment_create_request_dto.dart';
import '../shared/dto/chore/request/chore_create_request_dto.dart';
import '../shared/dto/chore/request/chore_assignment_filter_request_dto.dart';
import '../shared/dto/chore/request/chore_status_update_request_dto.dart';
import '../shared/utils/types/date_range.dart';
import '../shared/enums/chore_status.dart';

class ChoreProvider extends ChangeNotifier {
  final ChoreService _choreService;

  AssignmentOverviewDTO? _overview;
  List<ChoreAssignmentResponseDTO> _assignments = [];
  List<ChoreResponseDTO> _householdChores = [];
  bool _isLoading = false;
  String? _errorMessage;

  ChoreProvider({required ApiClient apiClient}) : _choreService = ChoreService(apiClient);

  AssignmentOverviewDTO? get overview => _overview;
  List<ChoreAssignmentResponseDTO> get assignments => _assignments;
  List<ChoreResponseDTO> get householdChores => _householdChores;
  bool get isLoading => _isLoading;
  String? get errorMessage => _errorMessage;

  Future<void> loadAssignments({
    List<ChoreStatus>? statuses,
    String? assigneeId,
    String? descriptionContains,
    DateRange? dateRange,
  }) async {
    try {
      _isLoading = true;
      _errorMessage = null;
      notifyListeners();

      // Fetch overview and filtered assignments
      // Satisfying the backend's @NotNull requirement for dateRange
      final range = dateRange ??
          DateRange(
            startDate: DateTime.now().subtract(const Duration(days: 30)),
            endDate: DateTime.now().add(const Duration(days: 30)),
          );

      final results = await Future.wait([
        _choreService.getUserAssignmentOverview(),
        _choreService.getFilteredChoreAssignments(ChoreAssignmentFilterRequestDTO(
          statuses: statuses,
          assigneeId: assigneeId,
          descriptionContains: descriptionContains,
          dateRange: range,
        )),
        _choreService.getAllHouseholdChores(),
      ]);

      _overview = results[0] as AssignmentOverviewDTO;
      _assignments = results[1] as List<ChoreAssignmentResponseDTO>;
      _householdChores = results[2] as List<ChoreResponseDTO>;

    } on ApiException catch (e) {
      _errorMessage = e.message;
    } catch (e) {
      _errorMessage = "An unexpected error occurred while fetching chore assignments.";
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<bool> updateAssignmentStatus(String assignmentId, ChoreStatus newStatus) async {
    try {
      _isLoading = true;
      _errorMessage = null;
      notifyListeners();

      final request = ChoreStatusUpdateRequestDTO(newStatus: newStatus);
      await _choreService.updateChoreAssignmentStatus(assignmentId, request);
      
      // We don't reload everything, just refresh the overview and assignments 
      // but keeping current filters would be better. For simplicity now, just reload.
      await loadAssignments(); 
      return true;
    } on ApiException catch (e) {
      _errorMessage = e.message;
      return false;
    } catch (e) {
      _errorMessage = "An unexpected error occurred while updating status.";
      return false;
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> loadHouseholdChores() async {
    try {
      _isLoading = true;
      _errorMessage = null;
      notifyListeners();

      _householdChores = await _choreService.getAllHouseholdChores();
    } on ApiException catch (e) {
      _errorMessage = e.message;
    } catch (e) {
      _errorMessage = "An unexpected error occurred while fetching chores.";
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<bool> createChore(String description, int frequencyDays, String householdId) async {
    try {
      _isLoading = true;
      _errorMessage = null;
      notifyListeners();

      final request = ChoreCreateRequestDTO(
        description: description,
        frequencyDays: frequencyDays,
        householdId: householdId,
      );
      await _choreService.createChore(request);
      await loadHouseholdChores();
      return true;
    } on ApiException catch (e) {
      _errorMessage = e.message;
      return false;
    } catch (e) {
      _errorMessage = "An unexpected error occurred while creating chore.";
      return false;
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<bool> deleteChore(String choreId) async {
    try {
      _isLoading = true;
      _errorMessage = null;
      notifyListeners();

      await _choreService.deleteChore(choreId);
      await loadHouseholdChores();
      return true;
    } on ApiException catch (e) {
      _errorMessage = e.message;
      return false;
    } catch (e) {
      _errorMessage = "An unexpected error occurred while deleting chore.";
      return false;
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<bool> deleteAssignment(String assignmentId) async {
    try {
      _isLoading = true;
      _errorMessage = null;
      notifyListeners();

      await _choreService.deleteChoreAssignment(assignmentId);
      await loadAssignments();
      return true;
    } on ApiException catch (e) {
      _errorMessage = e.message;
      return false;
    } catch (e) {
      _errorMessage = "An unexpected error occurred while deleting assignment.";
      return false;
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<bool> createAssignment(String choreId, String userId, DateTime dueDate) async {
    try {
      _isLoading = true;
      _errorMessage = null;
      notifyListeners();

      final request = ChoreAssignmentCreateRequestDTO(
        choreId: choreId,
        assignedUserId: userId,
        dueDate: dueDate,
      );
      await _choreService.createAssignment(request);
      await loadAssignments();
      return true;
    } on ApiException catch (e) {
      _errorMessage = e.message;
      return false;
    } catch (e) {
      _errorMessage = "An unexpected error occurred while creating assignment.";
      return false;
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<bool> createChoreAndAssignment(String description, String assigneeId, DateTime? dueDate, String householdId) async {
    try {
      _isLoading = true;
      _errorMessage = null;
      notifyListeners();

      // 1. Create the Chore
      final choreRequest = ChoreCreateRequestDTO(
        description: description,
        frequencyDays: 0, // 0 for non-periodical as per UI context usually
        householdId: householdId,
      );
      final chore = await _choreService.createChore(choreRequest);

      // 2. Create the Assignment
      final assignmentRequest = ChoreAssignmentCreateRequestDTO(
        choreId: chore.id,
        assignedUserId: assigneeId,
        dueDate: dueDate,
      );
      await _choreService.createAssignment(assignmentRequest);

      // 3. Refresh list
      await loadAssignments();
      return true;
    } on ApiException catch (e) {
      _errorMessage = e.message;
      return false;
    } catch (e) {
      _errorMessage = "An unexpected error occurred while creating assignment.";
      return false;
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  void clear() {
    _overview = null;
    _assignments = [];
    _householdChores = [];
    _isLoading = false;
    _errorMessage = null;
    notifyListeners();
  }
}
