// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chore_assignment_response_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChoreAssignmentResponseDTO _$ChoreAssignmentResponseDTOFromJson(
  Map<String, dynamic> json,
) => ChoreAssignmentResponseDTO(
  assignmentId: json['assignmentId'] as String,
  choreId: json['choreId'] as String,
  choreDescription: json['choreDescription'] as String,
  assignedUser: UserResponseDTO.fromJson(
    json['assignedUser'] as Map<String, dynamic>,
  ),
  dueDate: json['dueDate'] == null
      ? null
      : DateTime.parse(json['dueDate'] as String),
  status: $enumDecode(_$ChoreStatusEnumMap, json['status']),
);

Map<String, dynamic> _$ChoreAssignmentResponseDTOToJson(
  ChoreAssignmentResponseDTO instance,
) => <String, dynamic>{
  'assignmentId': instance.assignmentId,
  'choreId': instance.choreId,
  'choreDescription': instance.choreDescription,
  'assignedUser': instance.assignedUser.toJson(),
  'dueDate': instance.dueDate?.toIso8601String(),
  'status': _$ChoreStatusEnumMap[instance.status]!,
};

const _$ChoreStatusEnumMap = {
  ChoreStatus.pending: 'PENDING',
  ChoreStatus.completed: 'COMPLETED',
  ChoreStatus.overdue: 'OVERDUE',
};
