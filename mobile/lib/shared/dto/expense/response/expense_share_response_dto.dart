import 'package:json_annotation/json_annotation.dart';

part 'expense_share_response_dto.g.dart';

@JsonSerializable(explicitToJson: true)
class ExpenseShareResponseDTO {
  final String id;
  final String userId;
  final String userFullName;
  @JsonKey(fromJson: _numToDouble, toJson: _doubleToNum)
  final double amount;

  ExpenseShareResponseDTO({
    required this.id,
    required this.userId,
    required this.userFullName,
    required this.amount,
  });

  factory ExpenseShareResponseDTO.fromJson(Map<String, dynamic> json) =>
      _$ExpenseShareResponseDTOFromJson(json);

  Map<String, dynamic> toJson() => _$ExpenseShareResponseDTOToJson(this);

  static double _numToDouble(num val) => val.toDouble();
  static num _doubleToNum(double val) => val;
}
