// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chore_status_update_request_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChoreStatusUpdateRequestDTO _$ChoreStatusUpdateRequestDTOFromJson(
  Map<String, dynamic> json,
) => ChoreStatusUpdateRequestDTO(
  newStatus: $enumDecode(_$ChoreStatusEnumMap, json['newStatus']),
);

Map<String, dynamic> _$ChoreStatusUpdateRequestDTOToJson(
  ChoreStatusUpdateRequestDTO instance,
) => <String, dynamic>{'newStatus': _$ChoreStatusEnumMap[instance.newStatus]!};

const _$ChoreStatusEnumMap = {
  ChoreStatus.pending: 'PENDING',
  ChoreStatus.completed: 'COMPLETED',
  ChoreStatus.overdue: 'OVERDUE',
};
