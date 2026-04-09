package com.assetmanagement.backend.mapper;

import com.assetmanagement.backend.entity.Transaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface TransactionMapper {
    void insert(Transaction transaction);
    List<Transaction> findRecent(int limit);
    List<Transaction> findByAssetId(Long assetId);
    // 현재 대여 중인 자산의 최신 RENT 트랜잭션 대여자/반납일 수정
    void updateLatestRent(@Param("assetId") Long assetId,
                          @Param("newUserId") Long newUserId,
                          @Param("newDueDate") LocalDate newDueDate);
}
