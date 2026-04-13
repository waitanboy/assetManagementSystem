package com.assetmanagement.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    private Long id;
    private Long assetId;
    private Long userId;
    private String type; // RENT, RETURN, CREATE, UPDATE, DELETE
    private LocalDateTime transactionDate;
    private LocalDate dueDate; // 반납 예정일 (RENT 시에만 사용)
    private String note;
    
    // Joined fields
    private String assetName;
    private String assetSerial;
    private String userEmail;
    private String userRole;
    private String userDepartment;
    
    private String signatureData; // Base64 signature image
    private String ocrData;
}
