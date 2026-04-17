package com.assetmanagement.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private Long id;
    private Long senderId;
    private Long receiverId; // Can be null for system messages
    private String content;
    private LocalDateTime timestamp;
    private boolean isRead;
    
    // Virtual fields for UI convenience
    private String senderName;
    private String senderEmail;
    private String receiverName;
}
