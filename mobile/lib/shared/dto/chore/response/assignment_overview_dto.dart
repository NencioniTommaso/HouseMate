import 'package:json_annotation/json_annotation.dart';

part 'assignment_overview_dto.g.dart';

@JsonSerializable(explicitToJson: true)
class AssignmentOverviewDTO {
  final int? pendingAssignments;
  final int? overdueAssignments;

  AssignmentOverviewDTO({
    this.pendingAssignments,
    this.overdueAssignments,
  });

  factory AssignmentOverviewDTO.fromJson(Map<String, dynamic> json) =>
      _$AssignmentOverviewDTOFromJson(json);

  Map<String, dynamic> toJson() => _$AssignmentOverviewDTOToJson(this);
}
