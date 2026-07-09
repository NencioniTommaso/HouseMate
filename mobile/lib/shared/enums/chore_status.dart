import 'package:json_annotation/json_annotation.dart';

enum ChoreStatus {
  @JsonValue('PENDING')
  pending,

  @JsonValue('COMPLETED')
  completed,

  @JsonValue('OVERDUE')
  overdue
}
