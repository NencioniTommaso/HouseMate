import 'package:flutter/material.dart';
import '../../shared/dto/user/response/user_response_dto.dart';

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
                  "${member.name} ${member.surname}",
                  style: const TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                Text(
                  member.email,
                  style: TextStyle(color: Colors.grey.shade600),
                ),
                if (member.iban != null)
                  Text(
                    member.iban!,
                    style: TextStyle(color: Colors.grey.shade600),
                  ),
                if (member.paymentLink != null)
                  Text(
                    member.paymentLink!,
                    style: const TextStyle(
                      color: Colors.blue,
                      decoration: TextDecoration.underline,
                    ),
                  ),
              ],
            ),
          ),
          if (isMe)
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
              decoration: BoxDecoration(
                color: Colors.green,
                borderRadius: BorderRadius.circular(4),
              ),
              child: const Text(
                "You",
                style: TextStyle(color: Colors.white),
              ),
            )
          else
            ElevatedButton(
              onPressed: isAdminMode ? onRemove : null,
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.red,
                foregroundColor: Colors.white,
              ),
              child: const Text("Remove"),
            ),
        ],
      ),
    );
  }
}
