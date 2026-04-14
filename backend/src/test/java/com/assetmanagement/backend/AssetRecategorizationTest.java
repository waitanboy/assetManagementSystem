package com.assetmanagement.backend;

import com.assetmanagement.backend.entity.Category;
import com.assetmanagement.backend.mapper.AssetMapper;
import com.assetmanagement.backend.mapper.CategoryMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

@SpringBootTest
public class AssetRecategorizationTest {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void recategorize() {
        // 1. Create new categories if they don't exist
        Long notebookId = getOrCreateCategory("노트북");
        Long tabletId = getOrCreateCategory("태블릿");
        Long pcId = getOrCreateCategory("PC/데스크탑");

        System.out.println("Categories: Notebook=" + notebookId + ", Tablet=" + tabletId + ", PC=" + pcId);

        // 2. Update Assets based on keywords
        
        // Notebook keywords
        updateAssetsByKeyword(new String[]{"MacBook", "ThinkPad", "Dell XPS", "LG Gram"}, notebookId);
        
        // Tablet keywords
        updateAssetsByKeyword(new String[]{"iPad", "Galaxy Tab"}, tabletId);
        
        // PC keywords (if any specific ones, or just generic PC markers)
        updateAssetsByKeyword(new String[]{"PC", "Workstation"}, pcId);

        System.out.println("✅ Recategorization completed.");
    }

    private Long getOrCreateCategory(String name) {
        List<Category> cats = categoryMapper.findAll();
        for (Category c : cats) {
            if (c.getName().equalsIgnoreCase(name)) return c.getId();
        }
        Category newCat = Category.builder().name(name).useOcr(false).build();
        categoryMapper.insert(newCat);
        return newCat.getId();
    }

    private void updateAssetsByKeyword(String[] keywords, Long newCategoryId) {
        for (String kw : keywords) {
            String sql = "UPDATE asset SET category_id = ? WHERE name LIKE ?";
            int count = jdbcTemplate.update(sql, newCategoryId, "%" + kw + "%");
            System.out.println("Updated " + count + " assets for keyword: " + kw + " -> Category " + newCategoryId);
        }
    }
}
