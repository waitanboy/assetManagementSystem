package com.assetmanagement.backend.mapper;

import com.assetmanagement.backend.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMessageMapper {
    void insert(ChatMessage message);
    
    List<ChatMessage> findByInterlocutors(@Param("user1") Long user1, @Param("user2") Long user2);
    
    List<ChatMessage> findRecentForAdmin();
    
    void markAsRead(@Param("messageId") Long messageId);
    
    void markAllAsRead(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);
    
    int countUnread(@Param("receiverId") Long receiverId);
}
