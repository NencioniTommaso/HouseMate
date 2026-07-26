import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../../shared/dto/chore/response/chore_assignment_response_dto.dart';
import '../../shared/enums/chore_status.dart';

class ChoreAssignmentItemElement extends StatelessWidget {
  final ChoreAssignmentResponseDTO assignment;
  final VoidCallback? onStatusToggle;

  const ChoreAssignmentItemElement({
    super.key,
    required this.assignment,
    this.onStatusToggle,
  });

  @override
  Widget build(BuildContext context) {
    final dateString = assignment.dueDate != null
        ? DateFormat('dd MMM').format(assignment.dueDate!)
        : 'No due date';

    Color statusColor;
    switch (assignment.status) {
      case ChoreStatus.completed:
        statusColor = Colors.green;
        break;
      case ChoreStatus.overdue:
        statusColor = Colors.red;
        break;
      case ChoreStatus.pending:
      default:
        statusColor = Colors.orange;
        break;
    }

    return Container(
      margin: const EdgeInsets.only(bottom: 8),
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: Colors.grey.shade300),
      ),
      child: Row(
        children: [
          Checkbox(
            value: assignment.status == ChoreStatus.completed,
            onChanged: (val) => onStatusToggle?.call(),
          ),
          const SizedBox(width: 8),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  assignment.choreDescription,
                  style: TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                    decoration: assignment.status == ChoreStatus.completed
                        ? TextDecoration.lineThrough
                        : null,
                  ),
                ),
                Text(
                  "Due: $dateString • Assigned to: ${assignment.assignedUser.name}",
                  style: TextStyle(color: Colors.grey.shade600, fontSize: 13),
                ),
              ],
            ),
          ),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
            decoration: BoxDecoration(
              color: statusColor.withOpacity(0.1),
              borderRadius: BorderRadius.circular(4),
              border: Border.all(color: statusColor),
            ),
            child: Text(
              assignment.status.name.toUpperCase(),
              style: TextStyle(
                color: statusColor,
                fontSize: 10,
                fontWeight: FontWeight.bold,
              ),
            ),
          ),
        ],
      ),
    );
  }
}
