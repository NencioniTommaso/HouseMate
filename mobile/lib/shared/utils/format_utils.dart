import 'package:intl/intl.dart';

class FormatUtils {
  static final _currencyFormatter = NumberFormat.currency(symbol: '€', decimalDigits: 2);
  static final _dateFormatter = DateFormat('dd/MM/yyyy');
  static final _dateTimeFormatter = DateFormat('dd/MM/yyyy HH:mm');

  static String formatCurrency(double amount) {
    return _currencyFormatter.format(amount);
  }

  static String formatDate(DateTime date) {
    return _dateFormatter.format(date);
  }

  static String formatDateTime(DateTime date) {
    return _dateTimeFormatter.format(date);
  }

  static String formatShortDate(DateTime date) {
    return DateFormat('d MMM').format(date);
  }

  // "22 Aug 15:15"
  static String formatShortDateTime(DateTime date) {
    return DateFormat('d MMM HH:mm').format(date);
  }

  // "Aug 22 09:00"
  static String formatExpenseDateTime(DateTime date) {
    return DateFormat('MMM d HH:mm').format(date);
  }
}
