// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'expense_response_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExpenseResponseDTO _$ExpenseResponseDTOFromJson(Map<String, dynamic> json) =>
    ExpenseResponseDTO(
      id: json['id'] as String,
      description: json['description'] as String,
      date: ExpenseResponseDTO._dateFromJson(json['date']),
      amount: ExpenseResponseDTO._numToDouble(json['amount'] as num),
      payerId: json['payerId'] as String,
      payerFullName: json['payerFullName'] as String,
      splitType: $enumDecode(_$ExpenseSplitTypeEnumMap, json['splitType']),
      householdId: json['householdId'] as String,
      shares: (json['shares'] as List<dynamic>)
          .map(
            (e) => ExpenseShareResponseDTO.fromJson(e as Map<String, dynamic>),
          )
          .toList(),
    );

Map<String, dynamic> _$ExpenseResponseDTOToJson(ExpenseResponseDTO instance) =>
    <String, dynamic>{
      'id': instance.id,
      'description': instance.description,
      'date': instance.date?.toIso8601String(),
      'amount': ExpenseResponseDTO._doubleToNum(instance.amount),
      'payerId': instance.payerId,
      'payerFullName': instance.payerFullName,
      'splitType': _$ExpenseSplitTypeEnumMap[instance.splitType]!,
      'householdId': instance.householdId,
      'shares': instance.shares.map((e) => e.toJson()).toList(),
    };

const _$ExpenseSplitTypeEnumMap = {
  ExpenseSplitType.equalSplit: 'EQUAL_SPLIT',
  ExpenseSplitType.shares: 'SHARES',
  ExpenseSplitType.exactAmount: 'EXACT_AMOUNT',
  ExpenseSplitType.adjustment: 'ADJUSTMENT',
};
