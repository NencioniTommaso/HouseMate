// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'debt_filter_request_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DebtFilterRequestDTO _$DebtFilterRequestDTOFromJson(
  Map<String, dynamic> json,
) => DebtFilterRequestDTO(
  userTransactionRole: $enumDecode(
    _$UserTransactionRoleEnumMap,
    json['userTransactionRole'],
  ),
  involvedId: json['involvedId'] as String?,
);

Map<String, dynamic> _$DebtFilterRequestDTOToJson(
  DebtFilterRequestDTO instance,
) => <String, dynamic>{
  'userTransactionRole':
      _$UserTransactionRoleEnumMap[instance.userTransactionRole]!,
  'involvedId': instance.involvedId,
};

const _$UserTransactionRoleEnumMap = {
  UserTransactionRole.CREDITOR: 'CREDITOR',
  UserTransactionRole.DEBTOR: 'DEBTOR',
  UserTransactionRole.ALL: 'ALL',
};
