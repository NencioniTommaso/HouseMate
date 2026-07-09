import 'package:json_annotation/json_annotation.dart';

part 'expense_share_response_dto.g.dart';

@JsonSerializable(explicitToJson: true)
class ExpenseShareResponseDTO {
  final String id;
  final String userId;
  final String userFullName;
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
}
