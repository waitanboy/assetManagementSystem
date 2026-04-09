package com.assetmanagement.backend.mapper;

import com.assetmanagement.backend.entity.RepairLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface RepairLogMapper {
    void insert(RepairLog repairLog);
    void update(RepairLog repairLog);
    RepairLog findById(Long id);
    RepairLog findActiveRepairByAssetId(Long assetId);
    List<RepairLog> findByAssetId(Long assetId);
    List<RepairLog> findAllRepairs();
}
