// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'debt_response_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DebtResponseDTO _$DebtResponseDTOFromJson(Map<String, dynamic> json) =>
    DebtResponseDTO(
      debtId: json['debtId'] as String,
      userTransactionRole: $enumDecode(
        _$UserTransactionRoleEnumMap,
        json['userTransactionRole'],
      ),
      involvedId: json['involvedId'] as String,
      involvedName: json['involvedName'] as String,
      amount: DebtResponseDTO._numToDouble(json['amount'] as num),
    );

Map<String, dynamic> _$DebtResponseDTOToJson(DebtResponseDTO instance) =>
    <String, dynamic>{
      'debtId': instance.debtId,
      'userTransactionRole':
          _$UserTransactionRoleEnumMap[instance.userTransactionRole]!,
      'involvedId': instance.involvedId,
      'involvedName': instance.involvedName,
      'amount': DebtResponseDTO._doubleToNum(instance.amount),
    };

const _$UserTransactionRoleEnumMap = {
  UserTransactionRole.creditor: 'CREDITOR',
  UserTransactionRole.debtor: 'DEBTOR',
  UserTransactionRole.all: 'ALL',
};
