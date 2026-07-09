import 'package:json_annotation/json_annotation.dart';

part 'settlement_create_request_dto.g.dart';

@JsonSerializable(explicitToJson: true)
class SettlementCreateRequestDTO {
  final String debtId;
  final String creditorId;
  final double amount;
  final String? description;

  SettlementCreateRequestDTO({
    required this.debtId,
    required this.creditorId,
    required this.amount,
    this.description,
  });

  factory SettlementCreateRequestDTO.fromJson(Map<String, dynamic> json) =>
      _$SettlementCreateRequestDTOFromJson(json);

  Map<String, dynamic> toJson() => _$SettlementCreateRequestDTOToJson(this);
}
