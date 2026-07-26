import 'package:flutter/material.dart';
import '../core/network/api_client.dart';
import '../core/network/api_exception.dart';
import '../services/chore_service.dart';
import '../shared/dto/chore/response/assignment_overview_dto.dart';
import '../shared/dto/chore/response/chore_assignment_response_dto.dart';
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

      // Fetch overview and filtered assignments (empty filter for all for now)
      final results = await Future.wait([
        _choreService.getUserAssignmentOverview(),
        _choreService.getFilteredChoreAssignments(ChoreAssignmentFilterRequestDTO(
          dateRange: DateRange(), // Assuming empty date range means all
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
}
