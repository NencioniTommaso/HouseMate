import 'package:json_annotation/json_annotation.dart';

part 'shopping_list_update_request_dto.g.dart';

@JsonSerializable(explicitToJson: true)
class ShoppingListUpdateRequestDTO {
  final List<bool> boughtItems;

  ShoppingListUpdateRequestDTO({
    required this.boughtItems,
  });

  factory ShoppingListUpdateRequestDTO.fromJson(Map<String, dynamic> json) =>
      _$ShoppingListUpdateRequestDTOFromJson(json);

  Map<String, dynamic> toJson() => _$ShoppingListUpdateRequestDTOToJson(this);
}
