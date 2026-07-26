import 'package:json_annotation/json_annotation.dart';

part 'user_net_overview_response_dto.g.dart';

@JsonSerializable(explicitToJson: true)
class UserNetOverviewResponseDTO {
  @JsonKey(fromJson: _numToDouble, toJson: _doubleToNum)
  final double actualCashFlowAmount;

  UserNetOverviewResponseDTO({
    required this.actualCashFlowAmount,
  });

  factory UserNetOverviewResponseDTO.fromJson(Map<String, dynamic> json) =>
      _$UserNetOverviewResponseDTOFromJson(json);

  Map<String, dynamic> toJson() => _$UserNetOverviewResponseDTOToJson(this);

  static double _numToDouble(num val) => val.toDouble();
  static num _doubleToNum(double val) => val;
}
