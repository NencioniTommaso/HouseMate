import 'package:json_annotation/json_annotation.dart';

enum UserTransactionRole {
  @JsonValue('CREDITOR')
  creditor,

  @JsonValue('DEBTOR')
  debtor,

  @JsonValue('ALL')
  all
}
