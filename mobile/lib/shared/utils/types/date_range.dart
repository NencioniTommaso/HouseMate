import 'package:json_annotation/json_annotation.dart';

part 'date_range.g.dart';

@JsonSerializable(explicitToJson: true)
class DateRange {
  final DateTime? startDate;
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
}
