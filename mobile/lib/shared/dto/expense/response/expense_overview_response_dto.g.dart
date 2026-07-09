// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'expense_overview_response_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExpenseOverviewResponseDTO _$ExpenseOverviewResponseDTOFromJson(
  Map<String, dynamic> json,
) => ExpenseOverviewResponseDTO(
  totalAmount: (json['totalAmount'] as num).toDouble(),
  expenseCount: (json['expenseCount'] as num?)?.toInt(),
);

Map<String, dynamic> _$ExpenseOverviewResponseDTOToJson(
  ExpenseOverviewResponseDTO instance,
) => <String, dynamic>{
  'totalAmount': instance.totalAmount,
  'expenseCount': instance.expenseCount,
};
