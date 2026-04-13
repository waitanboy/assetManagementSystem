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
public class RentalRequest {
    private Long id;
    private Long assetId;
    private Long userId;
    private LocalDateTime requestDate;
    private LocalDate plannedDueDate;
    private String purpose;
    private String status; // PENDING, APPROVED, REJECTED
    
    private Long processedBy;
    private LocalDateTime processDate;
    private String rejectReason;
    private String ocrData;

    // Joined fields
    private String assetName;
    private String assetSerial;
    private String userEmail;
    private String userName;
    private String userDepartment;
    private String processedByEmail;
    private boolean useOcr;
}
