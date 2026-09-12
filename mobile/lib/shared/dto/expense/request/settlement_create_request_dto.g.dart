// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'settlement_create_request_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SettlementCreateRequestDTO _$SettlementCreateRequestDTOFromJson(
  Map<String, dynamic> json,
) => SettlementCreateRequestDTO(
  debtId: json['debtId'] as String,
  creditorId: json['creditorId'] as String,
  amount: (json['amount'] as num).toDouble(),
  description: json['description'] as String?,
);

Map<String, dynamic> _$SettlementCreateRequestDTOToJson(
  SettlementCreateRequestDTO instance,
) => <String, dynamic>{
  'debtId': instance.debtId,
  'creditorId': instance.creditorId,
  'amount': instance.amount,
  'description': instance.description,
};
