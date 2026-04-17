package com.assetmanagement.backend.service;

import com.assetmanagement.backend.entity.ChatMessage;
import com.assetmanagement.backend.entity.User;
import com.assetmanagement.backend.mapper.ChatMessageMapper;
import com.assetmanagement.backend.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageMapper chatMessageMapper;
    private final UserMapper userMapper;
    private final SimpMessagingTemplate messagingTemplate;

    public static final String SYSTEM_ADMIN_EMAIL = "assetSysAdmin@asset.com";

    @Transactional
    public ChatMessage saveMessage(Long senderId, Long receiverId, String content) {
        ChatMessage message = ChatMessage.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .content(content)
                .timestamp(LocalDateTime.now())
                .isRead(false)
                .build();
        
        chatMessageMapper.insert(message);
        
        // Populate sender info for the response
        User sender = userMapper.findById(senderId);
        if (sender != null) {
            message.setSenderName(sender.getName());
            message.setSenderEmail(sender.getEmail());
        }
        
        return message;
    }

    public List<ChatMessage> getHistory(Long user1, Long user2) {
        return chatMessageMapper.findByInterlocutors(user1, user2);
    }

    public List<ChatMessage> getRecentConversations() {
        return chatMessageMapper.findRecentForAdmin();
    }

    @Transactional
    public void markAsRead(Long senderId, Long receiverId) {
        chatMessageMapper.markAllAsRead(senderId, receiverId);
    }

    public int getUnreadCount(Long userId) {
        return chatMessageMapper.countUnread(userId);
    }
    
    @Transactional
    public void sendSystemMessage(Long receiverId, String content) {
        if (receiverId == null) return;

        User systemAdmin = userMapper.findByEmail(SYSTEM_ADMIN_EMAIL);
        if (systemAdmin == null) {
            // Fallback: Find any admin with name "시스템 알림"
            systemAdmin = userMapper.findAll().stream()
                .filter(u -> "SYSTEM".equals(u.getDepartment()) || "시스템 알림".equals(u.getName()))
                .findFirst().orElse(null);
        }

        if (systemAdmin == null) {
            System.err.println("System admin account not found for notification");
            return;
        }

        ChatMessage saved = saveMessage(systemAdmin.getId(), receiverId, content);

        // Send via WebSocket
        User receiver = userMapper.findById(receiverId);
        if (receiver != null) {
            try {
                messagingTemplate.convertAndSendToUser(
                        receiver.getEmail(),
                        "/queue/messages",
                        saved
                );
            } catch (Exception e) {
                System.err.println("WebSocket delivery failed: " + e.getMessage());
            }
        }
    }

    public User getAdminUser() {
        List<User> allUsers = userMapper.findAll();
        if (allUsers == null) return null;

        // Find the first user with role ADMIN (or ROLE_ADMIN) who is NOT the system admin
        return allUsers.stream()
                .filter(u -> u.getRole() != null && 
                        (u.getRole().equalsIgnoreCase("ADMIN") || u.getRole().equalsIgnoreCase("ROLE_ADMIN")) && 
                        !SYSTEM_ADMIN_EMAIL.equalsIgnoreCase(u.getEmail()))
                .findFirst()
                .orElse(null);
    }

    public User getSystemUser() {
        User sys = userMapper.findByEmail(SYSTEM_ADMIN_EMAIL);
        if (sys == null) {
            // Case-insensitive search fallback
            List<User> allUsers = userMapper.findAll();
            if (allUsers != null) {
                sys = allUsers.stream()
                    .filter(u -> SYSTEM_ADMIN_EMAIL.equalsIgnoreCase(u.getEmail()) || 
                                 "SYSTEM".equalsIgnoreCase(u.getDepartment()) || 
                                 "시스템 알림".equals(u.getName()))
                    .findFirst().orElse(null);
            }
        }
        return sys;
    }
}
