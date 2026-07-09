import 'package:json_annotation/json_annotation.dart';
import '../../../enums/expense_split_type.dart';
import 'expense_share_response_dto.dart';

part 'expense_response_dto.g.dart';

@JsonSerializable(explicitToJson: true)
class ExpenseResponseDTO {
  final String id;
  final String description;
  final DateTime? date;
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
}
