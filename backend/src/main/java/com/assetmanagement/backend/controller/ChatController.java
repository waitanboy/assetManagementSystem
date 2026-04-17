package com.assetmanagement.backend.controller;

import com.assetmanagement.backend.entity.ChatMessage;
import com.assetmanagement.backend.entity.User;
import com.assetmanagement.backend.mapper.UserMapper;
import com.assetmanagement.backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserMapper userMapper;

    /**
     * STOMP: /app/chat.send
     * Receives a message and routes it to the correct recipient via /user/{email}/queue/messages
     * NOTE: convertAndSendToUser routes by Principal.getName() which is the user's email.
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload Map<String, Object> payload) {
        Long senderId = Long.valueOf(payload.get("senderId").toString());
        Long receiverId = Long.valueOf(payload.get("receiverId").toString());
        String content = payload.get("content").toString();

        log.debug("Chat message from senderId={} to receiverId={}: {}", senderId, receiverId, content);

        ChatMessage saved = chatService.saveMessage(senderId, receiverId, content);

        // Lookup emails — STOMP user routing is based on Principal.getName() = email
        User receiver = userMapper.findById(receiverId);
        User sender = userMapper.findById(senderId);

        if (receiver != null) {
            log.debug("Routing message to receiver email={}", receiver.getEmail());
            messagingTemplate.convertAndSendToUser(
                    receiver.getEmail(),
                    "/queue/messages",
                    saved
            );
        }

        if (sender != null) {
            log.debug("Routing message back to sender email={}", sender.getEmail());
            // Send back to sender so their own message appears in real time
            messagingTemplate.convertAndSendToUser(
                    sender.getEmail(),
                    "/queue/messages",
                    saved
            );
        }
    }

    @GetMapping("/history/{otherUserId}")
    public List<ChatMessage> getHistory(@AuthenticationPrincipal String email, @PathVariable Long otherUserId) {
        User currentUser = userMapper.findByEmail(email);
        return chatService.getHistory(currentUser.getId(), otherUserId);
    }

    @GetMapping("/admin/conversations")
    public List<ChatMessage> getAdminConversations() {
        return chatService.getRecentConversations();
    }

    @PostMapping("/read/{senderId}")
    public void markAsRead(@AuthenticationPrincipal String email, @PathVariable Long senderId) {
        User currentUser = userMapper.findByEmail(email);
        chatService.markAsRead(senderId, currentUser.getId());
    }

    @GetMapping("/unread-count")
    public int getUnreadCount(@AuthenticationPrincipal String email) {
        User currentUser = userMapper.findByEmail(email);
        return chatService.getUnreadCount(currentUser.getId());
    }
    
    @GetMapping("/admin-user")
    public User getAdminUser() {
        return chatService.getAdminUser();
    }

    @GetMapping("/system-user")
    public User getSystemUser() {
        return chatService.getSystemUser();
    }
}

