// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chore_assignment_create_request_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChoreAssignmentCreateRequestDTO _$ChoreAssignmentCreateRequestDTOFromJson(
  Map<String, dynamic> json,
) => ChoreAssignmentCreateRequestDTO(
  choreId: json['choreId'] as String,
  assignedUserId: json['assignedUserId'] as String,
  dueDate: json['dueDate'] == null
      ? null
      : DateTime.parse(json['dueDate'] as String),
);

Map<String, dynamic> _$ChoreAssignmentCreateRequestDTOToJson(
  ChoreAssignmentCreateRequestDTO instance,
) => <String, dynamic>{
  'choreId': instance.choreId,
  'assignedUserId': instance.assignedUserId,
  'dueDate': instance.dueDate?.toIso8601String(),
};
