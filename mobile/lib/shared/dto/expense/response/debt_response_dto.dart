import 'package:json_annotation/json_annotation.dart';
import '../../../enums/user_transaction_role.dart';

part 'debt_response_dto.g.dart';

@JsonSerializable(explicitToJson: true)
class DebtResponseDTO {
  final String debtId;
  final UserTransactionRole userTransactionRole;
  final String involvedId;
  final String involvedName;
  @JsonKey(fromJson: _numToDouble, toJson: _doubleToNum)
  final double amount;

  DebtResponseDTO({
    required this.debtId,
    required this.userTransactionRole,
    required this.involvedId,
    required this.involvedName,
    required this.amount,
  });

  factory DebtResponseDTO.fromJson(Map<String, dynamic> json) =>
      _$DebtResponseDTOFromJson(json);

  Map<String, dynamic> toJson() => _$DebtResponseDTOToJson(this);

  static double _numToDouble(num val) => val.toDouble();
  static num _doubleToNum(double val) => val;
}
