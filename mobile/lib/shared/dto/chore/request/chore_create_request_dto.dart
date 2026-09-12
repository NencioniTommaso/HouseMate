import 'package:json_annotation/json_annotation.dart';

part 'chore_create_request_dto.g.dart';

@JsonSerializable(explicitToJson: true)
class ChoreCreateRequestDTO {
  final String description;
  final int frequencyDays;
  final String householdId;

  ChoreCreateRequestDTO({
    required this.description,
    required this.frequencyDays,
    required this.householdId,
  });

  factory ChoreCreateRequestDTO.fromJson(Map<String, dynamic> json) =>
      _$ChoreCreateRequestDTOFromJson(json);

  Map<String, dynamic> toJson() => _$ChoreCreateRequestDTOToJson(this);
}
