import 'package:json_annotation/json_annotation.dart';

enum ShoppingListStatus {
  @JsonValue('NOT_STARTED')
  notStarted,

  @JsonValue('IN_PROGRESS')
  inProgress,

  @JsonValue('COMPLETED')
  completed,
}
