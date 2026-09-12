import 'package:json_annotation/json_annotation.dart';

part 'expense_overview_response_dto.g.dart';

@JsonSerializable(explicitToJson: true)
class ExpenseOverviewResponseDTO {
  @JsonKey(fromJson: _numToDouble, toJson: _doubleToNum)
  final double totalAmount;
  final int? expenseCount;

  ExpenseOverviewResponseDTO({
    required this.totalAmount,
    this.expenseCount,
  });

  factory ExpenseOverviewResponseDTO.fromJson(Map<String, dynamic> json) =>
      _$ExpenseOverviewResponseDTOFromJson(json);

  Map<String, dynamic> toJson() => _$ExpenseOverviewResponseDTOToJson(this);

  static double _numToDouble(num val) => val.toDouble();
  static num _doubleToNum(double val) => val;
}
