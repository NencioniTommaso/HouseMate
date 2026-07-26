import 'package:json_annotation/json_annotation.dart';

part 'date_range.g.dart';

@JsonSerializable(explicitToJson: true)
class DateRange {
  @JsonKey(fromJson: _dateFromJson)
  final DateTime? startDate;
  @JsonKey(fromJson: _dateFromJson)
  final DateTime? endDate;

  DateRange({
    this.startDate,
    this.endDate,
  }) {
    if (startDate != null && endDate != null && startDate!.isAfter(endDate!)) {
      throw ArgumentError("Start date must be before end date");
    }
  }

  factory DateRange.fromJson(Map<String, dynamic> json) =>
      _$DateRangeFromJson(json);

  Map<String, dynamic> toJson() => _$DateRangeToJson(this);

  static DateTime? _dateFromJson(dynamic json) {
    if (json == null) return null;
    if (json is String) return DateTime.parse(json);
    if (json is List) {
      return DateTime(
        json[0] as int,
        json[1] as int,
        json[2] as int,
        json.length > 3 ? json[3] as int : 0,
        json.length > 4 ? json[4] as int : 0,
        json.length > 5 ? json[5] as int : 0,
      );
    }
    return null;
  }
}
