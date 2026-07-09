import 'package:json_annotation/json_annotation.dart';
import '../../../enums/expense_split_type.dart';
import 'expense_share_request_dto.dart';

part 'expense_create_request.g.dart';

@JsonSerializable(explicitToJson: true)
class ExpenseCreateRequestDTO {
  final String description;
  final double amount;
  final ExpenseSplitType splitType;
  final List<ExpenseShareRequestDTO> shares;

  ExpenseCreateRequestDTO({
    required this.description,
    required this.amount,
    required this.splitType,
    required this.shares,
  });

  factory ExpenseCreateRequestDTO.fromJson(Map<String, dynamic> json) =>
      _$ExpenseCreateRequestDTOFromJson(json);

  Map<String, dynamic> toJson() => _$ExpenseCreateRequestDTOToJson(this);
}
