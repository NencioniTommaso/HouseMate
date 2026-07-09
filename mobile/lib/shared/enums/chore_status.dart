import 'package:json_annotation/json_annotation.dart';

enum ChoreStatus {
  @JsonValue('PENDING')
  PENDING,
  @JsonValue('COMPLETED')
  COMPLETED,
  @JsonValue('OVERDUE')
  OVERDUE
}
