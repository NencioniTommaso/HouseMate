import 'package:flutter/material.dart';

class HouseholdScreen extends StatelessWidget {
  const HouseholdScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.grey.shade100,
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            const SizedBox(height: 20),
            const Text(
              "Your Household",
              style: TextStyle(
                fontSize: 24,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 10),
            const Text(
              "The Cool House", // Placeholder for lblHouseholdName
              style: TextStyle(fontSize: 18),
            ),
            const SizedBox(height: 30),
            _buildLargeButton(context, "Manage Members", Icons.group),
            const SizedBox(height: 15),
            _buildLargeButton(context, "Chores List", Icons.list_alt),
            const SizedBox(height: 15),
            _buildLargeButton(context, "Shopping Lists", Icons.shopping_cart),
            const SizedBox(height: 15),
            _buildLargeButton(context, "Invite Member", Icons.person_add),
          ],
        ),
      ),
    );
  }

  Widget _buildLargeButton(BuildContext context, String text, IconData icon) {
    return SizedBox(
      width: double.infinity,
      height: 80,
      child: ElevatedButton(
        onPressed: () {},
        style: ElevatedButton.styleFrom(
          backgroundColor: Colors.white,
          foregroundColor: Colors.black,
          side: BorderSide(color: Colors.grey.shade300),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(8),
          ),
        ),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(icon, size: 28),
            const SizedBox(width: 15),
            Text(
              text,
              style: const TextStyle(fontSize: 18),
            ),
          ],
        ),
      ),
    );
  }
}
