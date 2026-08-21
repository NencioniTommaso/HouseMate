import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../../shared/dto/chore/response/chore_assignment_response_dto.dart';
import '../../shared/enums/chore_status.dart';

class ChoreAssignmentItemElement extends StatelessWidget {
  final ChoreAssignmentResponseDTO assignment;
  final String currentUserId;
  final bool isAdmin;
  final VoidCallback? onStatusToggle;
  final VoidCallback? onDelete;

  const ChoreAssignmentItemElement({
    super.key,
    required this.assignment,
    required this.currentUserId,
    required this.isAdmin,
    this.onStatusToggle,
    this.onDelete,
  });

  @override
  Widget build(BuildContext context) {
    final dateString = assignment.dueDate != null
        ? DateFormat('dd MMM').format(assignment.dueDate!)
        : 'No due date';

    Color statusColor;
    switch (assignment.status) {
      case ChoreStatus.completed:
        statusColor = const Color(0xFF4CAF50);
        break;
      case ChoreStatus.overdue:
        statusColor = const Color(0xFFE74C3C);
        break;
      case ChoreStatus.pending:
        statusColor = const Color(0xFFE67E22);
        break;
    }

    final bool isAssignedToMe = assignment.assignedUser.id == currentUserId;

    Widget content = Container(
      margin: const EdgeInsets.only(bottom: 8),
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
          Checkbox(
            value: assignment.status == ChoreStatus.completed,
            activeColor: const Color(0xFF3498DB),
            onChanged: (isAssignedToMe && assignment.status != ChoreStatus.completed)
                ? (val) => onStatusToggle?.call()
                : null,
          ),
          const SizedBox(width: 8),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  assignment.choreDescription,
                  style: const TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                    color: Color(0xFF2C3E50),
                  ),
                ),
                Text(
                  "Due: $dateString\nAssigned to: ${assignment.assignedUser.name}",
                  style: const TextStyle(color: Color(0xFF95A5A6), fontSize: 11),
                ),
              ],
            ),
          ),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
            decoration: BoxDecoration(
              color: statusColor.withValues(alpha: 0.1),
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

    if (isAdmin) {
      return Dismissible(
        key: Key(assignment.assignmentId),
        direction: DismissDirection.endToStart,
        confirmDismiss: (direction) async {
          onDelete?.call();
          return false; // We handle deletion via the dialog callback, so don't dismiss yet.
        },
        background: Container(
          margin: const EdgeInsets.only(bottom: 8),
          decoration: BoxDecoration(
            color: Colors.red,
            borderRadius: BorderRadius.circular(8),
          ),
          alignment: Alignment.centerRight,
          padding: const EdgeInsets.only(right: 20),
          child: const Icon(Icons.delete, color: Colors.white),
        ),
        child: content,
      );
    }

    return content;
  }
}
