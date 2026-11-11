import 'package:json_annotation/json_annotation.dart';

part 'expense_share_request_dto.g.dart';

@JsonSerializable(explicitToJson: true)
class ExpenseShareRequestDTO {
  final String userId;
  final double? share;

  ExpenseShareRequestDTO({
    required this.userId,
    this.share,
  });

  factory ExpenseShareRequestDTO.fromJson(Map<String, dynamic> json) =>
      _$ExpenseShareRequestDTOFromJson(json);

  Map<String, dynamic> toJson() => _$ExpenseShareRequestDTOToJson(this);
}
