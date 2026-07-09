import 'package:json_annotation/json_annotation.dart';

enum MessageType {
  @JsonValue('SUCCESS')
  SUCCESS,
  @JsonValue('ERROR')
  ERROR,
  @JsonValue('INFO')
  INFO
}
