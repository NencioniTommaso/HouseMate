// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'settlement_response_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SettlementResponseDTO _$SettlementResponseDTOFromJson(
  Map<String, dynamic> json,
) => SettlementResponseDTO(
  settlementId: json['settlementId'] as String,
  userTransactionRole: $enumDecode(
    _$UserTransactionRoleEnumMap,
    json['userTransactionRole'],
  ),
  involvedId: json['involvedId'] as String,
  involvedName: json['involvedName'] as String,
  amount: SettlementResponseDTO._numToDouble(json['amount'] as num),
  date: SettlementResponseDTO._dateFromJson(json['date']),
  description: json['description'] as String?,
  householdId: json['householdId'] as String,
);

Map<String, dynamic> _$SettlementResponseDTOToJson(
  SettlementResponseDTO instance,
) => <String, dynamic>{
  'settlementId': instance.settlementId,
  'userTransactionRole':
      _$UserTransactionRoleEnumMap[instance.userTransactionRole]!,
  'involvedId': instance.involvedId,
  'involvedName': instance.involvedName,
  'amount': SettlementResponseDTO._doubleToNum(instance.amount),
  'date': instance.date?.toIso8601String(),
  'description': instance.description,
  'householdId': instance.householdId,
};

const _$UserTransactionRoleEnumMap = {
  UserTransactionRole.creditor: 'CREDITOR',
  UserTransactionRole.debtor: 'DEBTOR',
  UserTransactionRole.all: 'ALL',
};
