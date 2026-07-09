import 'package:json_annotation/json_annotation.dart';

enum ShoppingListStatus {
  @JsonValue('NOT_STARTED')
  NOT_STARTED,
  @JsonValue('IN_PROGRESS')
  IN_PROGRESS,
  @JsonValue('COMPLETED')
  COMPLETED,
}
