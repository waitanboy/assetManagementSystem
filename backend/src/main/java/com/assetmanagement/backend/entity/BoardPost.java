package com.assetmanagement.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardPost {
    private Long id;
    private String title;
    private String content;
    private Long authorId;
    private int viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // join fields
    private String authorName;
    private String authorEmail;
}
