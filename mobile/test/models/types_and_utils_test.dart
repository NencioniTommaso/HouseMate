import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/shared/enums/expense_split_type.dart';
import 'package:mobile/shared/utils/expense_split_calculator.dart';
import 'package:mobile/shared/utils/format_utils.dart';
import 'package:mobile/shared/utils/types/date_range.dart';
import 'package:mobile/shared/utils/types/list_item.dart';
import 'package:mobile/shared/utils/validation/validation_patterns.dart';

void main() {
  group('ListItem', () {
    test('instantiates with defaults and custom values', () {
      final itemDefault = ListItem(itemName: 'Milk');
      expect(itemDefault.itemName, 'Milk');
      expect(itemDefault.isBought, isFalse);

      final itemCustom = ListItem(itemName: 'Bread', isBought: true);
      expect(itemCustom.itemName, 'Bread');
      expect(itemCustom.isBought, isTrue);

      itemCustom.isBought = false;
      expect(itemCustom.isBought, isFalse);
    });

    test('serializes and deserializes correctly', () {
      final item = ListItem(itemName: 'Eggs', isBought: true);
      final json = item.toJson();
      expect(json, {
        'itemName': 'Eggs',
        'bought': true,
      });

      final deserialized = ListItem.fromJson(json);
      expect(deserialized.itemName, 'Eggs');
      expect(deserialized.isBought, isTrue);
    });

    test('deserializes missing bought as false', () {
      final json = {'itemName': 'Cheese'};
      final deserialized = ListItem.fromJson(json);
      expect(deserialized.itemName, 'Cheese');
      expect(deserialized.isBought, isFalse);
    });
  });

  group('DateRange', () {
    test('instantiates correctly when start is before or equal to end', () {
      final start = DateTime(2026, 1, 1);
      final end = DateTime(2026, 1, 31);
      final range = DateRange(startDate: start, endDate: end);
      expect(range.startDate, start);
      expect(range.endDate, end);

      final sameDayRange = DateRange(startDate: start, endDate: start);
      expect(sameDayRange.startDate, start);
      expect(sameDayRange.endDate, start);
    });

    test('instantiates correctly with nulls', () {
      final range1 = DateRange();
      expect(range1.startDate, isNull);
      expect(range1.endDate, isNull);

      final range2 = DateRange(startDate: DateTime(2026, 1, 1));
      expect(range2.startDate, isNotNull);
      expect(range2.endDate, isNull);

      final range3 = DateRange(endDate: DateTime(2026, 1, 31));
      expect(range3.startDate, isNull);
      expect(range3.endDate, isNotNull);
    });

    test('throws ArgumentError when start is after end', () {
      final start = DateTime(2026, 2, 1);
      final end = DateTime(2026, 1, 1);
      expect(
        () => DateRange(startDate: start, endDate: end),
        throwsA(isA<ArgumentError>()),
      );
    });

    test('serializes and deserializes with ISO strings and lists', () {
      final start = DateTime.parse('2026-03-01T00:00:00.000Z');
      final end = DateTime.parse('2026-03-31T23:59:59.000Z');
      final range = DateRange(startDate: start, endDate: end);

      final json = range.toJson();
      expect(json['startDate'], start.toIso8601String());
      expect(json['endDate'], end.toIso8601String());

      final fromIso = DateRange.fromJson(json);
      expect(fromIso.startDate, start);
      expect(fromIso.endDate, end);

      final fromList = DateRange.fromJson({
        'startDate': [2026, 3, 1],
        'endDate': [2026, 3, 31, 23, 59, 59],
      });
      expect(fromList.startDate, DateTime(2026, 3, 1));
      expect(fromList.endDate, DateTime(2026, 3, 31, 23, 59, 59));
    });
  });

  group('ExpenseSplitCalculator', () {
    const memberIds = ['user1', 'user2', 'user3'];

    test('returns 0 for all members when totalAmount <= 0', () {
      final result = ExpenseSplitCalculator.calculate(
        totalAmount: 0,
        type: ExpenseSplitType.equalSplit,
        memberIds: memberIds,
        isIncluded: {},
        shareCounts: {},
        customAmounts: {},
        adjustmentAmounts: {},
      );

      expect(result, {'user1': 0.0, 'user2': 0.0, 'user3': 0.0});
    });

    test('calculates equalSplit correctly with all included', () {
      final result = ExpenseSplitCalculator.calculate(
        totalAmount: 90.0,
        type: ExpenseSplitType.equalSplit,
        memberIds: memberIds,
        isIncluded: {'user1': true, 'user2': true, 'user3': true},
        shareCounts: {},
        customAmounts: {},
        adjustmentAmounts: {},
      );

      expect(result['user1'], 30.0);
      expect(result['user2'], 30.0);
      expect(result['user3'], 30.0);
    });

    test('calculates equalSplit correctly with subset included', () {
      final result = ExpenseSplitCalculator.calculate(
        totalAmount: 60.0,
        type: ExpenseSplitType.equalSplit,
        memberIds: memberIds,
        isIncluded: {'user1': true, 'user2': false, 'user3': true},
        shareCounts: {},
        customAmounts: {},
        adjustmentAmounts: {},
      );

      expect(result['user1'], 30.0);
      expect(result['user2'], 0.0);
      expect(result['user3'], 30.0);
    });

    test('calculates equalSplit with zero included members', () {
      final result = ExpenseSplitCalculator.calculate(
        totalAmount: 60.0,
        type: ExpenseSplitType.equalSplit,
        memberIds: memberIds,
        isIncluded: {'user1': false, 'user2': false, 'user3': false},
        shareCounts: {},
        customAmounts: {},
        adjustmentAmounts: {},
      );

      expect(result, {'user1': 0.0, 'user2': 0.0, 'user3': 0.0});
    });

    test('calculates shares split correctly', () {
      final result = ExpenseSplitCalculator.calculate(
        totalAmount: 100.0,
        type: ExpenseSplitType.shares,
        memberIds: memberIds,
        isIncluded: {},
        shareCounts: {'user1': 1, 'user2': 3, 'user3': 0},
        customAmounts: {},
        adjustmentAmounts: {},
      );

      // total shares = 4 -> 25 per share
      expect(result['user1'], 25.0);
      expect(result['user2'], 75.0);
      expect(result['user3'], 0.0);
    });

    test('calculates shares split with zero total shares', () {
      final result = ExpenseSplitCalculator.calculate(
        totalAmount: 100.0,
        type: ExpenseSplitType.shares,
        memberIds: memberIds,
        isIncluded: {},
        shareCounts: {},
        customAmounts: {},
        adjustmentAmounts: {},
      );

      expect(result, {'user1': 0.0, 'user2': 0.0, 'user3': 0.0});
    });

    test('calculates exactAmount split correctly', () {
      final result = ExpenseSplitCalculator.calculate(
        totalAmount: 100.0,
        type: ExpenseSplitType.exactAmount,
        memberIds: memberIds,
        isIncluded: {},
        shareCounts: {},
        customAmounts: {'user1': 40.0, 'user2': 60.0},
        adjustmentAmounts: {},
      );

      expect(result['user1'], 40.0);
      expect(result['user2'], 60.0);
      expect(result['user3'], 0.0);
    });

    test('calculates adjustment split correctly', () {
      // total = 100. user1 adjustment = +10, user2 = -10, user3 = 0.
      // adjustmentSum = 0, remainder = 100. baseShare = 100/3 = 33.3333
      final result = ExpenseSplitCalculator.calculate(
        totalAmount: 90.0,
        type: ExpenseSplitType.adjustment,
        memberIds: memberIds,
        isIncluded: {'user1': true, 'user2': true, 'user3': true},
        shareCounts: {},
        customAmounts: {},
        adjustmentAmounts: {'user1': 15.0, 'user2': -15.0, 'user3': 0.0},
      );

      // remainder = 90 - 0 = 90. baseShare = 30.
      // user1: 30 + 15 = 45
      // user2: 30 - 15 = 15
      // user3: 30 + 0 = 30
      expect(result['user1'], 45.0);
      expect(result['user2'], 15.0);
      expect(result['user3'], 30.0);
    });

    test('calculates adjustment split with negative remainder or no included', () {
      final negativeRemainder = ExpenseSplitCalculator.calculate(
        totalAmount: 50.0,
        type: ExpenseSplitType.adjustment,
        memberIds: memberIds,
        isIncluded: {},
        shareCounts: {},
        customAmounts: {},
        adjustmentAmounts: {'user1': 60.0},
      );
      expect(negativeRemainder, {'user1': 0.0, 'user2': 0.0, 'user3': 0.0});

      final noneIncluded = ExpenseSplitCalculator.calculate(
        totalAmount: 50.0,
        type: ExpenseSplitType.adjustment,
        memberIds: memberIds,
        isIncluded: {'user1': false, 'user2': false, 'user3': false},
        shareCounts: {},
        customAmounts: {},
        adjustmentAmounts: {},
      );
      expect(noneIncluded, {'user1': 0.0, 'user2': 0.0, 'user3': 0.0});
    });

    test('validates split amounts matching vs mismatching total', () {
      expect(
        ExpenseSplitCalculator.validate(
          totalAmount: 0,
          type: ExpenseSplitType.equalSplit,
          memberIds: memberIds,
          calculatedShares: {'user1': 0.0},
        ),
        isNull,
      );

      expect(
        ExpenseSplitCalculator.validate(
          totalAmount: 100.0,
          type: ExpenseSplitType.equalSplit,
          memberIds: memberIds,
          calculatedShares: {'user1': 50.0, 'user2': 50.0},
        ),
        isNull,
      );

      final error = ExpenseSplitCalculator.validate(
        totalAmount: 100.0,
        type: ExpenseSplitType.equalSplit,
        memberIds: memberIds,
        calculatedShares: {'user1': 40.0, 'user2': 50.0},
      );
      expect(error, contains('Total split'));
      expect(error, contains('does not match total amount'));
    });
  });

  group('FormatUtils', () {
    test('formatCurrency formats with euro symbol', () {
      final formatted = FormatUtils.formatCurrency(24.5);
      expect(formatted, contains('€'));
      expect(formatted, contains('24'));
    });

    test('formatDate formats dd/MM/yyyy', () {
      final date = DateTime(2026, 11, 10);
      expect(FormatUtils.formatDate(date), '10/11/2026');
    });

    test('formatDateTime formats dd/MM/yyyy HH:mm', () {
      final date = DateTime(2026, 11, 10, 14, 35);
      expect(FormatUtils.formatDateTime(date), '10/11/2026 14:35');
    });

    test('formatShortDate formats d MMM', () {
      final date = DateTime(2026, 8, 22);
      expect(FormatUtils.formatShortDate(date), '22 Aug');
    });

    test('formatShortDateTime formats d MMM HH:mm', () {
      final date = DateTime(2026, 8, 22, 15, 15);
      expect(FormatUtils.formatShortDateTime(date), '22 Aug 15:15');
    });

    test('formatExpenseDateTime formats MMM d HH:mm', () {
      final date = DateTime(2026, 8, 22, 9, 0);
      expect(FormatUtils.formatExpenseDateTime(date), 'Aug 22 09:00');
    });
  });

  group('ValidationPatterns', () {
    test('email regex validates correctly', () {
      final regex = RegExp(ValidationPatterns.email);
      expect(regex.hasMatch('test@example.com'), isTrue);
      expect(regex.hasMatch('first.last+tag@sub.domain.org'), isTrue);
      expect(regex.hasMatch('plainaddress'), isFalse);
      expect(regex.hasMatch('@missingusername.com'), isFalse);
    });

    test('iban regex validates correctly', () {
      final regex = RegExp(ValidationPatterns.iban);
      expect(regex.hasMatch('NL91ABNA0417164300'), isTrue);
      expect(regex.hasMatch('DE89370400440532013000'), isTrue);
      expect(regex.hasMatch('invalid'), isFalse);
      expect(regex.hasMatch('12345'), isFalse);
    });

    test('paymentLink regex validates correctly', () {
      final regex = RegExp(ValidationPatterns.paymentLink);
      expect(regex.hasMatch('https://tikkie.me/pay/123'), isTrue);
      expect(regex.hasMatch('http://paypal.me/user'), isTrue);
      expect(regex.hasMatch('ftp://example.com'), isFalse);
      expect(regex.hasMatch('not_a_link'), isFalse);
    });
  });
}
