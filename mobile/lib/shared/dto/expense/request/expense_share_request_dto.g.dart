// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'expense_share_request_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExpenseShareRequestDTO _$ExpenseShareRequestDTOFromJson(
  Map<String, dynamic> json,
) => ExpenseShareRequestDTO(
  userId: json['userId'] as String,
  share: (json['share'] as num?)?.toDouble(),
);

Map<String, dynamic> _$ExpenseShareRequestDTOToJson(
  ExpenseShareRequestDTO instance,
) => <String, dynamic>{'userId': instance.userId, 'share': instance.share};
