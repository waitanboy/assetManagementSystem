package com.assetmanagement.backend.service;

import com.assetmanagement.backend.entity.Category;
import com.assetmanagement.backend.entity.Transaction;
import com.assetmanagement.backend.mapper.AssetMapper;
import com.assetmanagement.backend.mapper.CategoryMapper;
import com.assetmanagement.backend.mapper.TransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryMapper categoryMapper;
    private final AssetMapper assetMapper;
    private final TransactionMapper transactionMapper;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryMapper.findAll();
    }

    @Transactional(readOnly = true)
    public Category getCategoryById(Long id) {
        return categoryMapper.findById(id);
    }

    @Transactional
    public Category createCategory(Category category) {
        categoryMapper.insert(category);
        logActivity(null, "CREATE", "Category created: " + category.getName());
        return category;
    }

    @Transactional
    public void updateCategory(Long id, Category category) {
        Category oldCategory = categoryMapper.findById(id);
        category.setId(id);
        categoryMapper.update(category);

        String diffNote = "";
        if (oldCategory != null && !Objects.equals(oldCategory.getName(), category.getName())) {
            diffNote = " [변경내역: 이름: '" + oldCategory.getName() + "' → '" + category.getName() + "']";
        }
        logActivity(null, "UPDATE", "Category updated: " + category.getName() + diffNote);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryMapper.findById(id);
        if (category != null) {
            // Check if there are assets associated with this category
            int assetCount = assetMapper.countByCategoryId(id);
            if (assetCount > 0) {
                throw new RuntimeException("삭제하려는 카테고리에 연결된 자산이 " + assetCount + "개 존재합니다. 먼저 자산을 삭제하거나 카테고리를 변경해주세요.");
            }
            categoryMapper.delete(id);
            logActivity(null, "DELETE", "Category deleted: " + category.getName());
        }
    }

    private void logActivity(Long assetId, String type, String note) {
        Long actorId = userService.getCurrentUserId();
        if (actorId == null)
            return;

        Transaction tx = Transaction.builder()
                .assetId(assetId)
                .userId(actorId)
                .type(type)
                .note(note)
                .build();
        transactionMapper.insert(tx);
    }
}
