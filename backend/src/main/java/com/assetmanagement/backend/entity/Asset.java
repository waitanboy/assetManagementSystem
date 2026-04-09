package com.assetmanagement.backend.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asset {
    private Long id;
    
    private Long categoryId;
    
    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must be less than 255 characters")
    private String name;
    
    @NotBlank(message = "Serial number is required")
    @Size(max = 100, message = "Serial number must be less than 100 characters")
    private String serialNumber;
    
    private String status; // AVAILABLE, RENTED, REPAIRING
    
    @Size(max = 255, message = "Location must be less than 255 characters")
    private String location;
    
    @Size(max = 500, message = "Image URL must be less than 500 characters")
    private String imageUrl;
    
    private String useYn; // Y or N
    
    // 대여 중일 때 반납 예정일 (transaction JOIN 시 포함)
    private LocalDate dueDate;

    // 대여 중일 때 대여자 정보 (Admin 전용 표시용, transaction JOIN 시 포함)
    private String rentedByEmail;
    private String rentedByDepartment;
    private Long rentedByUserId;
}
