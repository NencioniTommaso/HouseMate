import 'package:flutter/material.dart';
import '../core/network/api_client.dart';
import '../core/network/api_exception.dart';
import '../services/chore_service.dart';
import '../shared/dto/chore/response/assignment_overview_dto.dart';
import '../shared/dto/chore/response/chore_assignment_response_dto.dart';
import '../shared/dto/chore/request/chore_assignment_create_request_dto.dart';
import '../shared/dto/chore/request/chore_create_request_dto.dart';
import '../shared/dto/chore/request/chore_assignment_filter_request_dto.dart';
import '../shared/utils/types/date_range.dart';

class ChoreProvider extends ChangeNotifier {
  final ChoreService _choreService = ChoreService(ApiClient());

  AssignmentOverviewDTO? _overview;
  List<ChoreAssignmentResponseDTO> _assignments = [];
  bool _isLoading = false;
  String? _errorMessage;

  AssignmentOverviewDTO? get overview => _overview;
  List<ChoreAssignmentResponseDTO> get assignments => _assignments;
  bool get isLoading => _isLoading;
  String? get errorMessage => _errorMessage;

  Future<void> loadAssignments() async {
    try {
      _isLoading = true;
      _errorMessage = null;
      notifyListeners();

      // Fetch overview and filtered assignments
      // Providing a wide DateRange to satisfy the backend's @NotNull requirement
      final now = DateTime.now();
      final defaultRange = DateRange(
        startDate: now.subtract(const Duration(days: 30)),
        endDate: now.add(const Duration(days: 30)),
      );

      final results = await Future.wait([
        _choreService.getUserAssignmentOverview(),
        _choreService.getFilteredChoreAssignments(ChoreAssignmentFilterRequestDTO(
          dateRange: defaultRange,
        )),
      ]);

      _overview = results[0] as AssignmentOverviewDTO;
      _assignments = results[1] as List<ChoreAssignmentResponseDTO>;

    } on ApiException catch (e) {
      _errorMessage = e.message;
    } catch (e) {
      _errorMessage = "An unexpected error occurred while fetching chore assignments.";
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
}
