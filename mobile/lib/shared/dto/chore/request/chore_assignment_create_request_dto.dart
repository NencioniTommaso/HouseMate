import 'package:json_annotation/json_annotation.dart';

part 'chore_assignment_create_request_dto.g.dart';

@JsonSerializable(explicitToJson: true)
class ChoreAssignmentCreateRequestDTO {
  final String choreId;
  final String assignedUserId;
  final DateTime? dueDate;

  ChoreAssignmentCreateRequestDTO({
    required this.choreId,
    required this.assignedUserId,
    this.dueDate,
  });

  factory ChoreAssignmentCreateRequestDTO.fromJson(Map<String, dynamic> json) =>
      _$ChoreAssignmentCreateRequestDTOFromJson(json);

  Map<String, dynamic> toJson() => _$ChoreAssignmentCreateRequestDTOToJson(this);
}
