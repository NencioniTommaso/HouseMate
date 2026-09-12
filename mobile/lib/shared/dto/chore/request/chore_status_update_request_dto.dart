import 'package:json_annotation/json_annotation.dart';
import '../../../enums/chore_status.dart';

part 'chore_status_update_request_dto.g.dart';

@JsonSerializable(explicitToJson: true)
class ChoreStatusUpdateRequestDTO {
  final ChoreStatus newStatus;

  ChoreStatusUpdateRequestDTO({
    required this.newStatus,
  });

  factory ChoreStatusUpdateRequestDTO.fromJson(Map<String, dynamic> json) =>
      _$ChoreStatusUpdateRequestDTOFromJson(json);

  Map<String, dynamic> toJson() => _$ChoreStatusUpdateRequestDTOToJson(this);
}
