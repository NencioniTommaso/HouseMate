// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chore_assignment_filter_request_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChoreAssignmentFilterRequestDTO _$ChoreAssignmentFilterRequestDTOFromJson(
  Map<String, dynamic> json,
) => ChoreAssignmentFilterRequestDTO(
  statuses: (json['statuses'] as List<dynamic>?)
      ?.map((e) => $enumDecode(_$ChoreStatusEnumMap, e))
      .toList(),
  assigneeId: json['assigneeId'] as String?,
  descriptionContains: json['descriptionContains'] as String?,
  dateRange: DateRange.fromJson(json['dateRange'] as Map<String, dynamic>),
);

Map<String, dynamic> _$ChoreAssignmentFilterRequestDTOToJson(
  ChoreAssignmentFilterRequestDTO instance,
) => <String, dynamic>{
  'statuses': instance.statuses?.map((e) => _$ChoreStatusEnumMap[e]!).toList(),
  'assigneeId': instance.assigneeId,
  'descriptionContains': instance.descriptionContains,
  'dateRange': instance.dateRange.toJson(),
};

const _$ChoreStatusEnumMap = {
  ChoreStatus.pending: 'PENDING',
  ChoreStatus.completed: 'COMPLETED',
  ChoreStatus.overdue: 'OVERDUE',
};
