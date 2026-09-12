import 'package:flutter/material.dart';
import '../../shared/dto/user/response/user_response_dto.dart';
import '../../shared/enums/expense_split_type.dart';

class MemberSplitBox extends StatelessWidget {
  final UserResponseDTO member;
  final ExpenseSplitType splitType;
  final double calculatedAmount;
  final bool isIncluded;
  final int shareCount;
  final double customAmount;
  final double adjustmentAmount;
  
  final ValueChanged<bool> onToggle;
  final ValueChanged<int> onSharesChanged;
  final ValueChanged<double> onAmountChanged;
  final ValueChanged<double> onAdjustmentChanged;

  const MemberSplitBox({
    super.key,
    required this.member,
    required this.splitType,
    required this.calculatedAmount,
    required this.isIncluded,
    required this.shareCount,
    required this.customAmount,
    required this.adjustmentAmount,
    required this.onToggle,
    required this.onSharesChanged,
    required this.onAmountChanged,
    required this.onAdjustmentChanged,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 130,
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: const Color(0xFFE0E0E0)),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.05),
            blurRadius: 5,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(
            member.name,
            textAlign: TextAlign.center,
            style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14, color: Color(0xFF2C3E50)),
          ),
          Text(
            member.surname,
            textAlign: TextAlign.center,
            style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14, color: Color(0xFF2C3E50)),
          ),
          const SizedBox(height: 12),
          Text(
            "€ ${calculatedAmount.toStringAsFixed(2)}",
            style: const TextStyle(
              fontSize: 18, 
              fontWeight: FontWeight.bold,
              color: Color(0xFF2C3E50),
            ),
          ),
          const SizedBox(height: 12),
          _buildControl(),
        ],
      ),
    );
  }

  Widget _buildControl() {
    switch (splitType) {
      case ExpenseSplitType.equalSplit:
        return Checkbox(
          value: isIncluded,
          onChanged: (val) => onToggle(val ?? false),
          activeColor: const Color(0xFF3498DB),
        );
      case ExpenseSplitType.shares:
        return Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            _buildSmallIconButton(Icons.remove, () {
              if (shareCount > 0) onSharesChanged(shareCount - 1);
            }),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 8.0),
              child: Text("$shareCount", style: const TextStyle(fontWeight: FontWeight.bold)),
            ),
            _buildSmallIconButton(Icons.add, () => onSharesChanged(shareCount + 1)),
          ],
        );
      case ExpenseSplitType.exactAmount:
        return _buildTextField(
          initialValue: customAmount > 0 ? customAmount.toStringAsFixed(2) : "",
          onChanged: (val) => onAmountChanged(double.tryParse(val) ?? 0.0),
          hint: "0.00",
        );
      case ExpenseSplitType.adjustment:
        return Column(
          children: [
            _buildTextField(
              initialValue: adjustmentAmount != 0 ? adjustmentAmount.toStringAsFixed(2) : "",
              onChanged: (val) => onAdjustmentChanged(double.tryParse(val) ?? 0.0),
              hint: "+/- 0.00",
            ),
            const SizedBox(height: 4),
            Checkbox(
              value: isIncluded,
              onChanged: (val) => onToggle(val ?? false),
              activeColor: const Color(0xFF3498DB),
              visualDensity: VisualDensity.compact,
            ),
          ],
        );
    }
  }

  Widget _buildSmallIconButton(IconData icon, VoidCallback onPressed) {
    return InkWell(
      onTap: onPressed,
      child: Container(
        padding: const EdgeInsets.all(4),
        decoration: BoxDecoration(
          color: Colors.grey.shade100,
          borderRadius: BorderRadius.circular(4),
          border: Border.all(color: Colors.grey.shade300),
        ),
        child: Icon(icon, size: 16, color: Colors.black87),
      ),
    );
  }

  Widget _buildTextField({required String initialValue, required ValueChanged<String> onChanged, required String hint}) {
    return SizedBox(
      height: 35,
      child: TextFormField(
        initialValue: initialValue,
        keyboardType: const TextInputType.numberWithOptions(decimal: true, signed: true),
        onChanged: onChanged,
        textAlign: TextAlign.center,
        style: const TextStyle(fontSize: 13),
        decoration: InputDecoration(
          hintText: hint,
          isDense: true,
          contentPadding: const EdgeInsets.symmetric(horizontal: 8, vertical: 8),
          border: OutlineInputBorder(borderRadius: BorderRadius.circular(4)),
        ),
      ),
    );
  }
}
