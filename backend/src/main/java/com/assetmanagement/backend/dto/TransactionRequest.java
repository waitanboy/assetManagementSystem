package com.assetmanagement.backend.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class TransactionRequest {
    private Long userId;
    private String note;
    private LocalDate dueDate; // 반납 예정일 (RENT 시에만 사용)
}
