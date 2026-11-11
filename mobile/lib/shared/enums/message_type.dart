import 'package:json_annotation/json_annotation.dart';

enum MessageType {
  @JsonValue('SUCCESS')
  success,

  @JsonValue('ERROR')
  error,

  @JsonValue('INFO')
  info
}
