import 'package:json_annotation/json_annotation.dart';
import '../../../utils/types/list_item.dart';

part 'shopping_list_create_request_dto.g.dart';

@JsonSerializable(explicitToJson: true)
class ShoppingListCreateRequestDTO {
  final String name;
  final List<ListItem> items;
  final String householdId;
  final DateTime creationDate;

  ShoppingListCreateRequestDTO({
    required this.name,
    required this.items,
    required this.householdId,
    required this.creationDate,
  });

  factory ShoppingListCreateRequestDTO.fromJson(Map<String, dynamic> json) =>
      _$ShoppingListCreateRequestDTOFromJson(json);

  Map<String, dynamic> toJson() => _$ShoppingListCreateRequestDTOToJson(this);
}
