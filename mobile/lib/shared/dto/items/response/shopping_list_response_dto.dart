import 'package:json_annotation/json_annotation.dart';
import '../../../enums/shopping_list_status.dart';
import '../../../utils/types/list_item.dart';

part 'shopping_list_response_dto.g.dart';

@JsonSerializable(explicitToJson: true)
class ShoppingListResponseDTO {
  final String id;
  final String name;
  final List<ListItem> items;
  final ShoppingListStatus status;
  final String householdId;
  final DateTime creationDate;

  ShoppingListResponseDTO({
    required this.id,
    required this.name,
    required this.items,
    required this.status,
    required this.householdId,
    required this.creationDate,
  });

  factory ShoppingListResponseDTO.fromJson(Map<String, dynamic> json) =>
      _$ShoppingListResponseDTOFromJson(json);

  Map<String, dynamic> toJson() => _$ShoppingListResponseDTOToJson(this);
}
