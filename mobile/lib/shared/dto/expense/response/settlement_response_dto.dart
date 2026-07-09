import 'package:json_annotation/json_annotation.dart';
import '../../../enums/user_transaction_role.dart';

part 'settlement_response_dto.g.dart';

@JsonSerializable(explicitToJson: true)
class SettlementResponseDTO {
  final String settlementId;
  final UserTransactionRole userTransactionRole;
  final String involvedId;
  final String involvedName;
  final double amount;
  final DateTime? date;
  final String? description;
  final String householdId;

  SettlementResponseDTO({
    required this.settlementId,
    required this.userTransactionRole,
    required this.involvedId,
    required this.involvedName,
    required this.amount,
    this.date,
    this.description,
    required this.householdId,
  });

  factory SettlementResponseDTO.fromJson(Map<String, dynamic> json) =>
      _$SettlementResponseDTOFromJson(json);

  Map<String, dynamic> toJson() => _$SettlementResponseDTOToJson(this);
}
