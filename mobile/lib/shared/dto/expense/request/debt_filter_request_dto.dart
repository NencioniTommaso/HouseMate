import 'package:json_annotation/json_annotation.dart';
import '../../../enums/user_transaction_role.dart';

part 'debt_filter_request_dto.g.dart';

@JsonSerializable(explicitToJson: true)
class DebtFilterRequestDTO {
  final UserTransactionRole userTransactionRole;
  final String? involvedId;

  DebtFilterRequestDTO({
    required this.userTransactionRole,
    this.involvedId,
  });

  factory DebtFilterRequestDTO.fromJson(Map<String, dynamic> json) =>
      _$DebtFilterRequestDTOFromJson(json);

  Map<String, dynamic> toJson() => _$DebtFilterRequestDTOToJson(this);
}
