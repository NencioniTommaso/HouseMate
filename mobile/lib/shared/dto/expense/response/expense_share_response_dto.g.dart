// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'expense_share_response_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExpenseShareResponseDTO _$ExpenseShareResponseDTOFromJson(
  Map<String, dynamic> json,
) => ExpenseShareResponseDTO(
  id: json['id'] as String,
  userId: json['userId'] as String,
  userFullName: json['userFullName'] as String,
  amount: (json['amount'] as num).toDouble(),
);

Map<String, dynamic> _$ExpenseShareResponseDTOToJson(
  ExpenseShareResponseDTO instance,
) => <String, dynamic>{
  'id': instance.id,
  'userId': instance.userId,
  'userFullName': instance.userFullName,
  'amount': instance.amount,
};
