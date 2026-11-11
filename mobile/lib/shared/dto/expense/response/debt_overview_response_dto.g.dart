// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'debt_overview_response_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DebtOverviewResponseDTO _$DebtOverviewResponseDTOFromJson(
  Map<String, dynamic> json,
) => DebtOverviewResponseDTO(
  totalOwedByMe: (json['totalOwedByMe'] as num).toDouble(),
  totalOwedToMe: (json['totalOwedToMe'] as num).toDouble(),
);

Map<String, dynamic> _$DebtOverviewResponseDTOToJson(
  DebtOverviewResponseDTO instance,
) => <String, dynamic>{
  'totalOwedByMe': instance.totalOwedByMe,
  'totalOwedToMe': instance.totalOwedToMe,
};
