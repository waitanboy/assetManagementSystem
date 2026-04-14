package com.assetmanagement.backend;

import com.assetmanagement.backend.entity.Asset;
import com.assetmanagement.backend.mapper.AssetMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AssetFilterTest {

    @Autowired
    private AssetMapper assetMapper;

    @Test
    public void testCategoryFilter() {
        // Test with categoryId 1
        List<Asset> assets1 = assetMapper.findAll(null, null, 1L);
        System.out.println("Assets with Category 1: " + assets1.size());
        for (Asset a : assets1) {
            assertEquals(1L, a.getCategoryId(), "Asset " + a.getId() + " has wrong categoryId");
        }

        // Test with categoryId 2
        List<Asset> assets2 = assetMapper.findAll(null, null, 2L);
        System.out.println("Assets with Category 2: " + assets2.size());
        for (Asset a : assets2) {
            assertEquals(2L, a.getCategoryId(), "Asset " + a.getId() + " has wrong categoryId");
        }
    }
}
