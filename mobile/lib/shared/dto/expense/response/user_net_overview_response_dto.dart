import 'package:json_annotation/json_annotation.dart';

part 'user_net_overview_response_dto.g.dart';

@JsonSerializable(explicitToJson: true)
class UserNetOverviewResponseDTO {
  final double actualCashFlowAmount;

  UserNetOverviewResponseDTO({
    required this.actualCashFlowAmount,
  });

  factory UserNetOverviewResponseDTO.fromJson(Map<String, dynamic> json) =>
      _$UserNetOverviewResponseDTOFromJson(json);

  Map<String, dynamic> toJson() => _$UserNetOverviewResponseDTOToJson(this);
}
