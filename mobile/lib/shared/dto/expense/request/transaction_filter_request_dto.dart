import 'package:json_annotation/json_annotation.dart';
import '../../../enums/user_transaction_role.dart';
import '../../../utils/types/date_range.dart';

part 'transaction_filter_request_dto.g.dart';

@JsonSerializable(explicitToJson: true)
class TransactionFilterRequestDTO {
  final String? householdId;
  final UserTransactionRole? userTransactionRole;
  final DateRange? dateRange;
  final String? description;

  TransactionFilterRequestDTO({
    this.householdId,
    this.userTransactionRole,
    this.dateRange,
    this.description,
  });

  factory TransactionFilterRequestDTO.fromJson(Map<String, dynamic> json) =>
      _$TransactionFilterRequestDTOFromJson(json);

  Map<String, dynamic> toJson() => _$TransactionFilterRequestDTOToJson(this);
}
