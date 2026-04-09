package com.assetmanagement.backend.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RepairRequest {
    private String reason;
    private BigDecimal estimatedCost;
    private BigDecimal finalCost;
    private String resolutionNote;
}
