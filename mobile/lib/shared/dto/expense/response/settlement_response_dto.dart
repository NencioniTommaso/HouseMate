import 'package:json_annotation/json_annotation.dart';
import '../../../enums/user_transaction_role.dart';

part 'settlement_response_dto.g.dart';

@JsonSerializable(explicitToJson: true)
class SettlementResponseDTO {
  final String settlementId;
  final UserTransactionRole userTransactionRole;
  final String involvedId;
  final String involvedName;
  @JsonKey(fromJson: _numToDouble, toJson: _doubleToNum)
  final double amount;
  @JsonKey(fromJson: _dateFromJson)
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

  static double _numToDouble(num val) => val.toDouble();
  static num _doubleToNum(double val) => val;

  static DateTime? _dateFromJson(dynamic json) {
    if (json == null) return null;
    if (json is String) return DateTime.parse(json);
    if (json is List) {
      return DateTime(
        json[0] as int,
        json[1] as int,
        json[2] as int,
        json.length > 3 ? json[3] as int : 0,
        json.length > 4 ? json[4] as int : 0,
        json.length > 5 ? json[5] as int : 0,
      );
    }
    return null;
  }
}
