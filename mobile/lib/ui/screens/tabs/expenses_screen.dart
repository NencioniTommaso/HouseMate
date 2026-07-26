import 'package:flutter/material.dart';

class ExpensesScreen extends StatelessWidget {
  const ExpensesScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.grey.shade100,
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Header
            Row(
              children: [
                const Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      "Expenses",
                      style: TextStyle(
                        fontSize: 24,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    Text(
                      "This Month:",
                      style: TextStyle(color: Colors.grey),
                    ),
                  ],
                ),
                const Spacer(),
                ElevatedButton(
                  onPressed: () {},
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.blue,
                    foregroundColor: Colors.white,
                  ),
                  child: const Text("+ Add Expense"),
                ),
              ],
            ),
            const SizedBox(height: 15),

            // Summary Cards
            Row(
              children: [
                _buildSummaryCard("You Owe", "€ 45.00", Colors.red),
                const SizedBox(width: 10),
                _buildSummaryCard("You Are Owed", "€ 120.50", Colors.green),
              ],
            ),
            const SizedBox(height: 15),

            // Filters Panel
            Container(
              padding: const EdgeInsets.all(15),
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(8),
                border: Border.all(color: Colors.grey.shade300),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      _buildRadioButton("Debtor", false),
                      _buildRadioButton("Creditor", false),
                      _buildRadioButton("All", true),
                    ],
                  ),
                  const SizedBox(height: 10),
                  Row(
                    children: [
                      Expanded(
                        child: _buildDatePicker("From..."),
                      ),
                      const SizedBox(width: 10),
                      Expanded(
                        child: _buildDatePicker("To..."),
                      ),
                    ],
                  ),
                  const SizedBox(height: 10),
                  const TextField(
                    decoration: InputDecoration(
                      hintText: "Description...",
                      border: OutlineInputBorder(),
                      contentPadding: EdgeInsets.symmetric(horizontal: 10),
                    ),
                  ),
                  const SizedBox(height: 10),
                  Row(
                    children: [
                      _buildRadioButton("Expenses", true),
                      _buildRadioButton("Settlements", false),
                    ],
                  ),
                ],
              ),
            ),
            const SizedBox(height: 15),

            // Search Results
            Row(
              children: [
                const Text(
                  "Search Results",
                  style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                ),
                const Spacer(),
                TextButton(
                  onPressed: () {},
                  child: const Text("Hide Filters"),
                ),
              ],
            ),
            const SizedBox(height: 10),
            // Placeholder for data Container
            const Column(
              children: [
                // ExpenseItemElements or SettlementItemElements
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSummaryCard(String title, String amount, Color amountColor) {
    return Expanded(
      child: Container(
        padding: const EdgeInsets.all(15),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(8),
          border: Border.all(color: Colors.grey.shade300),
        ),
        child: Column(
          children: [
            Text(
              title,
              style: const TextStyle(color: Colors.grey),
            ),
            const SizedBox(height: 5),
            Text(
              amount,
              style: TextStyle(
                fontSize: 20,
                fontWeight: FontWeight.bold,
                color: amountColor,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildRadioButton(String label, bool selected) {
    return Row(
      children: [
        Radio(value: selected, groupValue: true, onChanged: (v) {}),
        Text(label),
        const SizedBox(width: 5),
      ],
    );
  }

  Widget _buildDatePicker(String hint) {
    return TextField(
      readOnly: true,
      decoration: InputDecoration(
        hintText: hint,
        suffixIcon: const Icon(Icons.calendar_today),
        border: const OutlineInputBorder(),
        contentPadding: const EdgeInsets.symmetric(horizontal: 10),
      ),
      onTap: () {},
    );
  }
}
