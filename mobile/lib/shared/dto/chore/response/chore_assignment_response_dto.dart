import 'package:json_annotation/json_annotation.dart';
import '../../user/response/user_response_dto.dart';
import '../../../enums/chore_status.dart';

part 'chore_assignment_response_dto.g.dart';

@JsonSerializable(explicitToJson: true)
class ChoreAssignmentResponseDTO {
  final String assignmentId;
  final String choreId;
  final String choreDescription;
  final UserResponseDTO assignedUser;
  final DateTime? dueDate;
  final ChoreStatus status;

  ChoreAssignmentResponseDTO({
    required this.assignmentId,
    required this.choreId,
    required this.choreDescription,
    required this.assignedUser,
    this.dueDate,
    required this.status,
  });

  factory ChoreAssignmentResponseDTO.fromJson(Map<String, dynamic> json) =>
      _$ChoreAssignmentResponseDTOFromJson(json);

  Map<String, dynamic> toJson() => _$ChoreAssignmentResponseDTOToJson(this);
}
