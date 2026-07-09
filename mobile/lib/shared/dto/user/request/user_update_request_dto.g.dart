// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'user_update_request_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

UserUpdateRequestDTO _$UserUpdateRequestDTOFromJson(
  Map<String, dynamic> json,
) => UserUpdateRequestDTO(
  name: json['name'] as String?,
  surname: json['surname'] as String?,
  email: json['email'] as String?,
  iban: json['iban'] as String?,
  paymentLink: json['paymentLink'] as String?,
);

Map<String, dynamic> _$UserUpdateRequestDTOToJson(
  UserUpdateRequestDTO instance,
) => <String, dynamic>{
  'name': instance.name,
  'surname': instance.surname,
  'email': instance.email,
  'iban': instance.iban,
  'paymentLink': instance.paymentLink,
};
