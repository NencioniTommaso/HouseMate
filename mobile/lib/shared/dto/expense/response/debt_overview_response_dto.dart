import 'package:json_annotation/json_annotation.dart';

part 'debt_overview_response_dto.g.dart';

@JsonSerializable(explicitToJson: true)
class DebtOverviewResponseDTO {
  final double totalOwedByMe;
  final double totalOwedToMe;

  DebtOverviewResponseDTO({
    required this.totalOwedByMe,
    required this.totalOwedToMe,
  });

  factory DebtOverviewResponseDTO.fromJson(Map<String, dynamic> json) =>
      _$DebtOverviewResponseDTOFromJson(json);

  Map<String, dynamic> toJson() => _$DebtOverviewResponseDTOToJson(this);
}
