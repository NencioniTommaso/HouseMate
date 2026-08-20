import 'package:json_annotation/json_annotation.dart';

part 'list_item.g.dart';

@JsonSerializable(explicitToJson: true)
class ListItem {
  final String itemName;
  @JsonKey(name: 'bought')
  bool isBought;

  ListItem({
    required this.itemName,
    this.isBought = false,
  });

  factory ListItem.fromJson(Map<String, dynamic> json) =>
      _$ListItemFromJson(json);

  Map<String, dynamic> toJson() => _$ListItemToJson(this);
}
