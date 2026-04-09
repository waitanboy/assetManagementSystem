package com.assetmanagement.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepairLog {
    private Long id;
    private Long assetId;
    private Long reportedBy;
    private String reason;
    private BigDecimal estimatedCost;
    private BigDecimal finalCost;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
