import 'package:flutter/material.dart';
import '../../shared/dto/user/response/user_response_dto.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/app_spacing.dart';
import 'shared/app_card.dart';
import 'shared/app_button.dart';

class MemberItemElement extends StatelessWidget {
  final UserResponseDTO member;
  final VoidCallback onRemove;
  final bool isAdminMode;
  final String currentUserId;

  const MemberItemElement({
    super.key,
    required this.member,
    required this.onRemove,
    required this.isAdminMode,
    required this.currentUserId,
  });

  @override
  Widget build(BuildContext context) {
    final bool isMe = member.id == currentUserId;

    return AppCard(
      margin: const EdgeInsets.only(bottom: AppSpacing.s),
      padding: const EdgeInsets.all(AppSpacing.m),
      child: Row(
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  "${member.name} ${member.surname}",
                  style: const TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                    color: AppColors.textPrimary,
                  ),
                ),
                Text(
                  member.email,
                  style: const TextStyle(color: AppColors.textSecondary, fontSize: 11),
                ),
                if (member.iban != null)
                  Text(
                    member.iban!,
                    style: const TextStyle(color: AppColors.textSecondary, fontSize: 11),
                  ),
                if (member.paymentLink != null)
                  Text(
                    member.paymentLink!,
                    style: const TextStyle(
                      color: AppColors.secondary,
                      decoration: TextDecoration.underline,
                      fontSize: 11,
                    ),
                  ),
              ],
            ),
          ),
          if (isMe)
            Container(
              padding: const EdgeInsets.symmetric(horizontal: AppSpacing.m, vertical: AppSpacing.xs),
              decoration: BoxDecoration(
                color: AppColors.success,
                borderRadius: BorderRadius.circular(AppSpacing.radiusS),
              ),
              child: const Text(
                "You",
                style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
              ),
            )
          else
            AppButton(
              label: "Remove",
              variant: AppButtonVariant.destructive,
              onPressed: isAdminMode ? onRemove : null,
            ),
        ],
      ),
    );
  }
}
