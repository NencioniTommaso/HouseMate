import 'package:json_annotation/json_annotation.dart';

part 'chore_reassign_request_dto.g.dart';

@JsonSerializable(explicitToJson: true)
class ChoreReassignRequestDTO {
  final String newAssigneeId;

  ChoreReassignRequestDTO({
    required this.newAssigneeId,
  });

  factory ChoreReassignRequestDTO.fromJson(Map<String, dynamic> json) =>
      _$ChoreReassignRequestDTOFromJson(json);

  Map<String, dynamic> toJson() => _$ChoreReassignRequestDTOToJson(this);
}
