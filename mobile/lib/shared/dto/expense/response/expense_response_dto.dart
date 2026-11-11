import 'package:json_annotation/json_annotation.dart';
import '../../../enums/expense_split_type.dart';
import 'expense_share_response_dto.dart';

part 'expense_response_dto.g.dart';

@JsonSerializable(explicitToJson: true)
class ExpenseResponseDTO {
  final String id;
  final String description;
  @JsonKey(fromJson: _dateFromJson)
  final DateTime? date;
  @JsonKey(fromJson: _numToDouble, toJson: _doubleToNum)
  final double amount;
  final String payerId;
  final String payerFullName;
  final ExpenseSplitType splitType;
  final String householdId;
  final List<ExpenseShareResponseDTO> shares;

  ExpenseResponseDTO({
    required this.id,
    required this.description,
    this.date,
    required this.amount,
    required this.payerId,
    required this.payerFullName,
    required this.splitType,
    required this.householdId,
    required this.shares,
  });

  factory ExpenseResponseDTO.fromJson(Map<String, dynamic> json) =>
      _$ExpenseResponseDTOFromJson(json);

  Map<String, dynamic> toJson() => _$ExpenseResponseDTOToJson(this);

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
