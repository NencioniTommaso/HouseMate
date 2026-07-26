import 'package:flutter/material.dart';
import '../../shared/dto/chore/response/chore_response_dto.dart';

class ChoreItemElement extends StatelessWidget {
  final ChoreResponseDTO chore;
  final VoidCallback onDelete;
  final bool isAdminMode;

  const ChoreItemElement({
    super.key,
    required this.chore,
    required this.onDelete,
    required this.isAdminMode,
  });

  String _getFrequencyText(int days) {
    if (days == 0) return "Frequency: not periodical";
    if (days == 1) return "Frequency: every day";
    return "Frequency: every $days days";
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: Colors.grey.shade300),
      ),
      child: Row(
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  chore.description,
                  style: const TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  _getFrequencyText(chore.frequencyDays),
                  style: TextStyle(
                    fontSize: 14,
                    color: Colors.grey.shade600,
                  ),
                ),
              ],
            ),
          ),
          ElevatedButton(
            onPressed: isAdminMode ? onDelete : null,
            style: ElevatedButton.styleFrom(
              backgroundColor: Colors.red,
              foregroundColor: Colors.white,
            ),
            child: const Text("Delete"),
          ),
        ],
      ),
    );
  }
}
