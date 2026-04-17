package com.assetmanagement.backend.mapper;

import com.assetmanagement.backend.entity.Asset;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AssetMapper {
    List<Asset> findAll(@Param("search") String search, @Param("status") String status, @Param("categoryId") Long categoryId);
    Asset findById(@Param("id") Long id);
    void insert(Asset asset);
    void insertBulk(@Param("assets") List<Asset> assets);
    void update(Asset asset);
    void delete(@Param("id") Long id);
    java.util.Map<String, Object> getAssetStats();
    List<Asset> findMyRentedAssets(@Param("userId") Long userId);
    List<Asset> findOverdueAssets();
    int countByCategoryId(@Param("categoryId") Long categoryId);
}
