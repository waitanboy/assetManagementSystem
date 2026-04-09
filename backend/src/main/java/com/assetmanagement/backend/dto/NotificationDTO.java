package com.assetmanagement.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {
    private String id; // A local ID for frontend mapping
    private String type; // PENDING_USER, OVERDUE_ASSET, MY_RENTED_ASSET, NEW_NOTICE
    private String title;
    private String message;
    private String link;
    private String timestamp;
    
    @JsonProperty("isRead")
    private boolean isRead;
}
