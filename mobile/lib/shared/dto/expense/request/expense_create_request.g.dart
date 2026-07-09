// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'expense_create_request.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExpenseCreateRequestDTO _$ExpenseCreateRequestDTOFromJson(
  Map<String, dynamic> json,
) => ExpenseCreateRequestDTO(
  description: json['description'] as String,
  amount: (json['amount'] as num).toDouble(),
  splitType: $enumDecode(_$ExpenseSplitTypeEnumMap, json['splitType']),
  shares: (json['shares'] as List<dynamic>)
      .map((e) => ExpenseShareRequestDTO.fromJson(e as Map<String, dynamic>))
      .toList(),
);

Map<String, dynamic> _$ExpenseCreateRequestDTOToJson(
  ExpenseCreateRequestDTO instance,
) => <String, dynamic>{
  'description': instance.description,
  'amount': instance.amount,
  'splitType': _$ExpenseSplitTypeEnumMap[instance.splitType]!,
  'shares': instance.shares.map((e) => e.toJson()).toList(),
};

const _$ExpenseSplitTypeEnumMap = {
  ExpenseSplitType.equalSplit: 'EQUAL_SPLIT',
  ExpenseSplitType.shares: 'SHARES',
  ExpenseSplitType.exactAmount: 'EXACT_AMOUNT',
  ExpenseSplitType.adjustment: 'ADJUSTMENT',
};
