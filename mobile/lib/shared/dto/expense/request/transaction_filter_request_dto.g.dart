// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'transaction_filter_request_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TransactionFilterRequestDTO _$TransactionFilterRequestDTOFromJson(
  Map<String, dynamic> json,
) => TransactionFilterRequestDTO(
  householdId: json['householdId'] as String?,
  userTransactionRole: $enumDecodeNullable(
    _$UserTransactionRoleEnumMap,
    json['userTransactionRole'],
  ),
  dateRange: json['dateRange'] == null
      ? null
      : DateRange.fromJson(json['dateRange'] as Map<String, dynamic>),
  description: json['description'] as String?,
);

Map<String, dynamic> _$TransactionFilterRequestDTOToJson(
  TransactionFilterRequestDTO instance,
) => <String, dynamic>{
  'householdId': instance.householdId,
  'userTransactionRole':
      _$UserTransactionRoleEnumMap[instance.userTransactionRole],
  'dateRange': instance.dateRange?.toJson(),
  'description': instance.description,
};

const _$UserTransactionRoleEnumMap = {
  UserTransactionRole.creditor: 'CREDITOR',
  UserTransactionRole.debtor: 'DEBTOR',
  UserTransactionRole.all: 'ALL',
};
