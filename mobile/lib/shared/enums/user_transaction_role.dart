import 'package:json_annotation/json_annotation.dart';

enum UserTransactionRole {
  @JsonValue('CREDITOR')
  CREDITOR,
  @JsonValue('DEBTOR')
  DEBTOR,
  @JsonValue('ALL')
  ALL
}
