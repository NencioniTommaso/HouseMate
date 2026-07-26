import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../../shared/dto/user/response/user_response_dto.dart';
import '../../shared/enums/expense_split_type.dart';

class MemberSplitBox extends StatefulWidget {
  final UserResponseDTO member;
  final ExpenseSplitType splitType;
  final double amount; // Current calculated amount to display

  const MemberSplitBox({
    super.key,
    required this.member,
    required this.splitType,
    this.amount = 0.00,
  });

  @override
  State<MemberSplitBox> createState() => _MemberSplitBoxState();
}

class _MemberSplitBoxState extends State<MemberSplitBox> {
  late TextEditingController _customAmountController;
  int _shareCount = 0;
  bool _isIncluded = false;

  @override
  void initState() {
    super.initState();
    _customAmountController = TextEditingController();
    if (widget.splitType == ExpenseSplitType.shares ||
        widget.splitType == ExpenseSplitType.exactAmount) {
      _isIncluded = true;
    }
  }

  @override
  void dispose() {
    _customAmountController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 120,
      padding: const EdgeInsets.all(8),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: Colors.grey.shade300),
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(
            "${widget.member.name}\n${widget.member.surname}",
            textAlign: TextAlign.center,
            style: const TextStyle(fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 8),
          Text(
            "€ ${widget.amount.toStringAsFixed(2)}",
            style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 8),
          if (widget.splitType == ExpenseSplitType.shares)
            _buildSpinner()
          else if (widget.splitType == ExpenseSplitType.exactAmount ||
              widget.splitType == ExpenseSplitType.adjustment)
            _buildTextField()
          else if (widget.splitType == ExpenseSplitType.equalSplit)
            _buildCheckbox(),
        ],
      ),
    );
  }

  Widget _buildSpinner() {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        IconButton(
          icon: const Icon(Icons.remove),
          onPressed: () {
            setState(() {
              if (_shareCount > 0) _shareCount--;
            });
          },
        ),
        Text("$_shareCount"),
        IconButton(
          icon: const Icon(Icons.add),
          onPressed: () {
            setState(() {
              _shareCount++;
            });
          },
        ),
      ],
    );
  }

  Widget _buildTextField() {
    return TextField(
      controller: _customAmountController,
      keyboardType: const TextInputType.numberWithOptions(decimal: true),
      inputFormatters: [
        FilteringTextInputFormatter.allow(RegExp(r'^\d*\.?\d{0,2}')),
      ],
      decoration: const InputDecoration(
        isDense: true,
        border: OutlineInputBorder(),
        contentPadding: EdgeInsets.symmetric(horizontal: 8, vertical: 8),
      ),
    );
  }

  Widget _buildCheckbox() {
    return Checkbox(
      value: _isIncluded,
      onChanged: (val) {
        setState(() {
          _isIncluded = val ?? false;
        });
      },
    );
  }
}
