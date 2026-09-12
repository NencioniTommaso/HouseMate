import 'package:json_annotation/json_annotation.dart';
import '../../../enums/chore_status.dart';
import '../../../utils/types/date_range.dart';

part 'chore_assignment_filter_request_dto.g.dart';

@JsonSerializable(explicitToJson: true)
class ChoreAssignmentFilterRequestDTO {
  final List<ChoreStatus>? statuses;
  final String? assigneeId;
  final String? descriptionContains;
  final DateRange dateRange;

  ChoreAssignmentFilterRequestDTO({
    this.statuses,
    this.assigneeId,
    this.descriptionContains,
    required this.dateRange,
  });

  factory ChoreAssignmentFilterRequestDTO.fromJson(Map<String, dynamic> json) =>
      _$ChoreAssignmentFilterRequestDTOFromJson(json);

  Map<String, dynamic> toJson() => _$ChoreAssignmentFilterRequestDTOToJson(this);
}
