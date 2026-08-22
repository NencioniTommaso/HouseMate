import '../enums/expense_split_type.dart';

class ExpenseSplitCalculator {
  static Map<String, double> calculate({
    required double totalAmount,
    required ExpenseSplitType type,
    required List<String> memberIds,
    required Map<String, bool> isIncluded,
    required Map<String, int> shareCounts,
    required Map<String, double> customAmounts,
    required Map<String, double> adjustmentAmounts,
  }) {
    if (totalAmount <= 0) {
      return {for (var id in memberIds) id: 0.0};
    }

    final Map<String, double> calculated = {};

    switch (type) {
      case ExpenseSplitType.equalSplit:
        final included = memberIds.where((id) => isIncluded[id] ?? true).toList();
        if (included.isEmpty) {
          for (var id in memberIds) calculated[id] = 0.0;
        } else {
          final share = totalAmount / included.length;
          for (var id in memberIds) {
            calculated[id] = (isIncluded[id] ?? true) ? share : 0.0;
          }
        }
        break;

      case ExpenseSplitType.shares:
        int totalShares = 0;
        for (var id in memberIds) {
          totalShares += shareCounts[id] ?? 0;
        }
        if (totalShares == 0) {
          for (var id in memberIds) calculated[id] = 0.0;
        } else {
          final pricePerShare = totalAmount / totalShares;
          for (var id in memberIds) {
            calculated[id] = pricePerShare * (shareCounts[id] ?? 0);
          }
        }
        break;

      case ExpenseSplitType.exactAmount:
        for (var id in memberIds) {
          calculated[id] = customAmounts[id] ?? 0.0;
        }
        break;

      case ExpenseSplitType.adjustment:
        double adjustmentSum = 0;
        for (var id in memberIds) {
          adjustmentSum += adjustmentAmounts[id] ?? 0.0;
        }
        
        final remainder = totalAmount - adjustmentSum;
        final included = memberIds.where((id) => isIncluded[id] ?? true).toList();
        
        if (included.isEmpty || remainder < 0) {
           for (var id in memberIds) calculated[id] = 0.0;
        } else {
          final baseShare = remainder / included.length;
          for (var id in memberIds) {
            if (isIncluded[id] ?? true) {
              final val = baseShare + (adjustmentAmounts[id] ?? 0.0);
              calculated[id] = val < 0 ? 0.0 : val;
            } else {
              calculated[id] = 0.0;
            }
          }
        }
        break;
    }

    return calculated;
  }

  static String? validate({
    required double totalAmount,
    required ExpenseSplitType type,
    required List<String> memberIds,
    required Map<String, double> calculatedShares,
  }) {
    if (totalAmount <= 0) return null;

    final double calculatedSum = calculatedShares.values.fold(0.0, (sum, val) => sum + val);
    
    if ((calculatedSum - totalAmount).abs() > 0.01) {
      return "Total split (€ ${calculatedSum.toStringAsFixed(2)}) does not match total amount (€ ${totalAmount.toStringAsFixed(2)})";
    }

    return null;
  }
}
