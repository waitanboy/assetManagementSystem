package com.assetmanagement.backend.mapper;

import com.assetmanagement.backend.entity.Transaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface TransactionMapper {
    void insert(Transaction transaction);
    List<Transaction> findRecent(@Param("limit") int limit);
    List<Transaction> findByAssetId(@Param("assetId") Long assetId);
    List<Transaction> findByUserId(@Param("userId") Long userId);
    void updateLatestRent(@Param("assetId") Long assetId,
                          @Param("newUserId") Long newUserId,
                          @Param("newDueDate") LocalDate newDueDate);
}
