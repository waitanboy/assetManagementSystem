package com.assetmanagement.backend.mapper;

import com.assetmanagement.backend.entity.RentalRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface RentalRequestMapper {
    void insert(RentalRequest request);
    
    RentalRequest findById(Long id);
    
    List<RentalRequest> findByStatus(String status);
    
    List<RentalRequest> findByUserId(Long userId);
    
    List<RentalRequest> findAll();
    
    void updateStatus(RentalRequest request);
}
