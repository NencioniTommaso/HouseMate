// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'user_net_overview_response_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

UserNetOverviewResponseDTO _$UserNetOverviewResponseDTOFromJson(
  Map<String, dynamic> json,
) => UserNetOverviewResponseDTO(
  actualCashFlowAmount: UserNetOverviewResponseDTO._numToDouble(
    json['actualCashFlowAmount'] as num,
  ),
);

Map<String, dynamic> _$UserNetOverviewResponseDTOToJson(
  UserNetOverviewResponseDTO instance,
) => <String, dynamic>{
  'actualCashFlowAmount': UserNetOverviewResponseDTO._doubleToNum(
    instance.actualCashFlowAmount,
  ),
};
