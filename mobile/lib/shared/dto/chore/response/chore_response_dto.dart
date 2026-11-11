import 'package:json_annotation/json_annotation.dart';

part 'chore_response_dto.g.dart';

@JsonSerializable(explicitToJson: true)
class ChoreResponseDTO {
  final String id;
  final String description;
  final int frequencyDays;

  ChoreResponseDTO({
    required this.id,
    required this.description,
    required this.frequencyDays,
  });

  factory ChoreResponseDTO.fromJson(Map<String, dynamic> json) =>
      _$ChoreResponseDTOFromJson(json);

  Map<String, dynamic> toJson() => _$ChoreResponseDTOToJson(this);
}
