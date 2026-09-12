import 'package:json_annotation/json_annotation.dart';

enum ExpenseSplitType {
  @JsonValue('EQUAL_SPLIT')
  equalSplit,

  @JsonValue('SHARES')
  shares,

  @JsonValue('EXACT_AMOUNT')
  exactAmount,

  @JsonValue('ADJUSTMENT')
  adjustment,
}