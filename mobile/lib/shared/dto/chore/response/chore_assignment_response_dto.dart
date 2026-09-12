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
  @JsonKey(fromJson: _dateFromJson)
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

  static DateTime? _dateFromJson(dynamic json) {
    if (json == null) return null;
    if (json is String) return DateTime.parse(json);
    if (json is List) {
      return DateTime(
        json[0] as int,
        json[1] as int,
        json[2] as int,
        json.length > 3 ? json[3] as int : 0,
        json.length > 4 ? json[4] as int : 0,
        json.length > 5 ? json[5] as int : 0,
      );
    }
    return null;
  }
}
