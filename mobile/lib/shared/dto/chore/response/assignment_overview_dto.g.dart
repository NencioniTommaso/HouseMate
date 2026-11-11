// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'assignment_overview_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AssignmentOverviewDTO _$AssignmentOverviewDTOFromJson(
  Map<String, dynamic> json,
) => AssignmentOverviewDTO(
  pendingAssignments: (json['pendingAssignments'] as num?)?.toInt(),
  overdueAssignments: (json['overdueAssignments'] as num?)?.toInt(),
);

Map<String, dynamic> _$AssignmentOverviewDTOToJson(
  AssignmentOverviewDTO instance,
) => <String, dynamic>{
  'pendingAssignments': instance.pendingAssignments,
  'overdueAssignments': instance.overdueAssignments,
};
