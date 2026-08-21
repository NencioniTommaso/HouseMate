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
        border: Border.all(color: const Color(0xFFE0E0E0)),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.05),
            blurRadius: 5,
            offset: const Offset(0, 2),
          ),
        ],
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
                    color: Color(0xFF2C3E50),
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  _getFrequencyText(chore.frequencyDays),
                  style: const TextStyle(
                    fontSize: 11,
                    color: Color(0xFF95A5A6),
                  ),
                ),
              ],
            ),
          ),
          ElevatedButton(
            onPressed: isAdminMode ? onDelete : null,
            style: ElevatedButton.styleFrom(
              backgroundColor: const Color(0xFFE74C3C),
              foregroundColor: Colors.white,
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(5)),
              elevation: 0,
            ),
            child: const Text("Delete"),
          ),
        ],
      ),
    );
  }
}
