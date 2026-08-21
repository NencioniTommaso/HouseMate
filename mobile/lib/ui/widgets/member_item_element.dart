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
                  "${member.name} ${member.surname}",
                  style: const TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                    color: Color(0xFF2C3E50),
                  ),
                ),
                Text(
                  member.email,
                  style: const TextStyle(color: Color(0xFF95A5A6), fontSize: 11),
                ),
                if (member.iban != null)
                  Text(
                    member.iban!,
                    style: const TextStyle(color: Color(0xFF95A5A6), fontSize: 11),
                  ),
                if (member.paymentLink != null)
                  Text(
                    member.paymentLink!,
                    style: const TextStyle(
                      color: Color(0xFF3498DB),
                      decoration: TextDecoration.underline,
                      fontSize: 11,
                    ),
                  ),
              ],
            ),
          ),
          if (isMe)
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
              decoration: BoxDecoration(
                color: const Color(0xFF4CAF50),
                borderRadius: BorderRadius.circular(5),
              ),
              child: const Text(
                "You",
                style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
              ),
            )
          else
            ElevatedButton(
              onPressed: isAdminMode ? onRemove : null,
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFFE74C3C),
                foregroundColor: Colors.white,
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(5)),
                elevation: 0,
              ),
              child: const Text("Remove"),
            ),
        ],
      ),
    );
  }
}
