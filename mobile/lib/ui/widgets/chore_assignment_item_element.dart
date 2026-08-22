import 'package:flutter/material.dart';
import '../../shared/dto/chore/response/chore_assignment_response_dto.dart';
import '../../shared/enums/chore_status.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_spacing.dart';
import '../../shared/utils/format_utils.dart';
import 'shared/app_card.dart';

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
        ? FormatUtils.formatShortDateTime(assignment.dueDate!)
        : 'No due date';

    Color statusColor;
    switch (assignment.status) {
      case ChoreStatus.completed:
        statusColor = AppColors.success;
        break;
      case ChoreStatus.overdue:
        statusColor = AppColors.danger;
        break;
      case ChoreStatus.pending:
        statusColor = AppColors.warning;
        break;
    }

    final bool isAssignedToMe = assignment.assignedUser.id == currentUserId;

    Widget content = AppCard(
      margin: const EdgeInsets.only(bottom: AppSpacing.s),
      padding: const EdgeInsets.all(AppSpacing.m),
      child: Row(
        children: [
          Checkbox(
            value: assignment.status == ChoreStatus.completed,
            activeColor: AppColors.secondary,
            onChanged: (isAssignedToMe && assignment.status != ChoreStatus.completed)
                ? (val) => onStatusToggle?.call()
                : null,
          ),
          const SizedBox(width: AppSpacing.s),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  assignment.choreDescription,
                  style: const TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                    color: AppColors.textPrimary,
                  ),
                ),
                const SizedBox(height: AppSpacing.xxs),
                Text(
                  "Due: $dateString\nAssigned to: ${assignment.assignedUser.name}",
                  style: const TextStyle(color: AppColors.textHint, fontSize: 11),
                ),
              ],
            ),
          ),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: AppSpacing.s, vertical: AppSpacing.xs),
            decoration: BoxDecoration(
              color: statusColor.withValues(alpha: 0.1),
              borderRadius: BorderRadius.circular(AppSpacing.radiusS),
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
          return false;
        },
        background: Container(
          margin: const EdgeInsets.only(bottom: AppSpacing.s),
          decoration: BoxDecoration(
            color: AppColors.danger,
            borderRadius: BorderRadius.circular(AppSpacing.radiusM),
          ),
          alignment: Alignment.centerRight,
          padding: const EdgeInsets.only(right: AppSpacing.l),
          child: const Icon(Icons.delete, color: Colors.white),
        ),
        child: content,
      );
    }

    return content;
  }
}
