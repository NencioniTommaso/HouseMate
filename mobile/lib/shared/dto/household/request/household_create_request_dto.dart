import 'package:json_annotation/json_annotation.dart';

part 'household_create_request_dto.g.dart';

@JsonSerializable(explicitToJson: true)
class HouseholdCreateRequestDTO {
  final String name;

  HouseholdCreateRequestDTO({
    required this.name,
  });

  factory HouseholdCreateRequestDTO.fromJson(Map<String, dynamic> json) =>
      _$HouseholdCreateRequestDTOFromJson(json);

  Map<String, dynamic> toJson() => _$HouseholdCreateRequestDTOToJson(this);
}
