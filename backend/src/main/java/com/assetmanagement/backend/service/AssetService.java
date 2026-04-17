package com.assetmanagement.backend.service;

import com.assetmanagement.backend.entity.Asset;
import com.assetmanagement.backend.entity.Transaction;
import com.assetmanagement.backend.mapper.AssetMapper;
import com.assetmanagement.backend.mapper.RepairLogMapper;
import com.assetmanagement.backend.mapper.TransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetMapper assetMapper;
    private final TransactionMapper transactionMapper;
    private final RepairLogMapper repairLogMapper;
    private final UserService userService;
    private final ChatService chatService;


    @Transactional(readOnly = true)
    public List<Asset> getAllAssets(String search, String status, Long categoryId) {
        return assetMapper.findAll(search, status, categoryId);
    }

    @Transactional(readOnly = true)
    public Asset getAssetById(Long id) {
        return assetMapper.findById(id);
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> getAssetStats() {
        return assetMapper.getAssetStats();
    }

    @Transactional(readOnly = true)
    public List<Asset> getMyRentedAssets() {
        Long userId = userService.getCurrentUserId();
        return getMyRentedAssets(userId);
    }

    @Transactional(readOnly = true)
    public List<Asset> getMyRentedAssets(Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        return assetMapper.findMyRentedAssets(userId);
    }

    @Transactional(readOnly = true)
    public List<com.assetmanagement.backend.entity.RepairLog> getAllRepairs() {
        return repairLogMapper.findAllRepairs();
    }

    @Transactional
    public Asset createAsset(Asset asset) {
        assetMapper.insert(asset);
        logActivity(asset.getId(), "CREATE", "New asset registered: " + asset.getName());
        return asset;
    }

    @Transactional
    public void createAssetsBulk(List<Asset> assets) {
        if (assets == null || assets.isEmpty()) {
            return;
        }
        assetMapper.insertBulk(assets);
        logActivity(null, "CREATE", assets.size() + " assets were bulk imported.");
    }

    @Transactional
    public void updateAsset(Long id, Asset asset) {
        Asset oldAsset = assetMapper.findById(id);
        asset.setId(id);
        assetMapper.update(asset);

        String diffNote = buildAssetDiff(oldAsset, asset);
        logActivity(id, "UPDATE", "Asset updated: " + asset.getName() + diffNote);
    }

    @Transactional
    public void deleteAsset(Long id) {
        Asset asset = assetMapper.findById(id);
        if (asset != null) {
            assetMapper.delete(id);
            logActivity(id, "DELETE", "Asset deleted: " + asset.getName());
        }
    }

    @Transactional
    public void rentAsset(Long assetId, Long userId, String note, java.time.LocalDate dueDate, String signatureData, String ocrData) {
        Asset asset = assetMapper.findById(assetId);
        if (asset == null) {
            throw new RuntimeException("Asset not found");
        }
        if (!"AVAILABLE".equals(asset.getStatus()) && !"REQUESTED".equals(asset.getStatus())) {
            throw new RuntimeException("Asset is currently " + asset.getStatus() + " and cannot be rented");
        }

        // Update status
        asset.setStatus("RENTED");
        assetMapper.update(asset);

        // Log transaction (Activity Log)
        Transaction transaction = Transaction.builder()
                .assetId(assetId)
                .userId(userId) // The subject who takes the asset
                .type("RENT")
                .dueDate(dueDate)
                .note(note)
                .signatureData(signatureData)
                .ocrData(ocrData)
                .build();
        transactionMapper.insert(transaction);

        // Send system chat notification
        try {
            String dateStr = dueDate != null ? dueDate.toString() : "미지정";
            String message = String.format("📢 자산 대여 안내\n- 대여 자산: %s\n- 반납 예정일: %s\n안전하게 사용 후 기한 내 반납 부탁드립니다.", 
                asset.getName(), dateStr);
            chatService.sendSystemMessage(userId, message);
        } catch (Exception e) {
            System.err.println("Failed to send rental chat notification: " + e.getMessage());
        }
    }

    @Transactional
    public void reassignRenter(Long assetId, Long newUserId, java.time.LocalDate newDueDate) {
        Asset asset = assetMapper.findById(assetId);
        if (asset == null) {
            throw new RuntimeException("Asset not found");
        }
        if (!"RENTED".equals(asset.getStatus())) {
            throw new RuntimeException("Asset is not currently rented");
        }

        // 최신 RENT 트랜잭션의 user_id와 due_date 수정
        transactionMapper.updateLatestRent(assetId, newUserId, newDueDate);

        // 변경 이력 기록
        Long actorId = userService.getCurrentUserId();
        Transaction log = Transaction.builder()
                .assetId(assetId)
                .userId(actorId != null ? actorId : newUserId)
                .type("UPDATE")
                .note("[대여자 변경] 새 대여자 ID: " + newUserId +
                        (newDueDate != null ? ", 반납 예정일: " + newDueDate : ""))
                .build();
        transactionMapper.insert(log);
    }

    @Transactional
    public void returnAsset(Long assetId, Long userId, String note, String signatureData) {
        Asset asset = assetMapper.findById(assetId);
        if (asset == null) {
            throw new RuntimeException("Asset not found");
        }
        if (!"RENTED".equals(asset.getStatus())) {
            throw new RuntimeException("Asset is not currently rented");
        }

        // Update status
        asset.setStatus("AVAILABLE");
        assetMapper.update(asset);

        // Log transaction (Activity Log)
        Transaction transaction = Transaction.builder()
                .assetId(assetId)
                .userId(userId) // The subject who returns the asset
                .type("RETURN")
                .note(note)
                .signatureData(signatureData)
                .build();
        transactionMapper.insert(transaction);
    }

    @Transactional
    public void startRepair(Long assetId, com.assetmanagement.backend.dto.RepairRequest request) {
        Asset asset = assetMapper.findById(assetId);
        if (asset == null) throw new RuntimeException("Asset not found");
        if (!"AVAILABLE".equals(asset.getStatus()) && !"RENTED".equals(asset.getStatus())) {
            throw new RuntimeException("Asset cannot be repaired from its current status.");
        }

        // Change status
        asset.setStatus("REPAIRING");
        assetMapper.update(asset);

        // Record repair log
        Long currentUserId = userService.getCurrentUserId();
        com.assetmanagement.backend.entity.RepairLog repairLog = com.assetmanagement.backend.entity.RepairLog.builder()
                .assetId(assetId)
                .reportedBy(currentUserId)
                .reason(request.getReason())
                .estimatedCost(request.getEstimatedCost())
                .status("IN_PROGRESS")
                .build();
        repairLogMapper.insert(repairLog);

        logActivity(assetId, "UPDATE", "[수리 시작] " + request.getReason());
    }

    @Transactional
    public void completeRepair(Long assetId, com.assetmanagement.backend.dto.RepairRequest request) {
        Asset asset = assetMapper.findById(assetId);
        if (asset == null) throw new RuntimeException("Asset not found");
        if (!"REPAIRING".equals(asset.getStatus())) {
            throw new RuntimeException("Asset is not currently repairing");
        }

        com.assetmanagement.backend.entity.RepairLog activeRepair = repairLogMapper.findActiveRepairByAssetId(assetId);
        if (activeRepair != null) {
            activeRepair.setFinalCost(request.getFinalCost());
            activeRepair.setStatus("COMPLETED");
            activeRepair.setEndDate(java.time.LocalDateTime.now());
            repairLogMapper.update(activeRepair);
        }

        // Change status back
        asset.setStatus("AVAILABLE");
        assetMapper.update(asset);

        logActivity(assetId, "UPDATE", "[수리 완료] 최종 비용: " + request.getFinalCost() + (request.getResolutionNote() != null ? " / 메모: " + request.getResolutionNote() : ""));
    }

    private void logActivity(Long assetId, String type, String note) {
        Long actorId = userService.getCurrentUserId();
        if (actorId == null) return;

        Transaction tx = Transaction.builder()
                .assetId(assetId)
                .userId(actorId)
                .type(type)
                .note(note)
                .build();
        transactionMapper.insert(tx);
    }

    private String buildAssetDiff(Asset oldAsset, Asset newAsset) {
        if (oldAsset == null) return "";
        List<String> changes = new ArrayList<>();

        if (!Objects.equals(oldAsset.getName(), newAsset.getName())) {
            changes.add("이름: '" + oldAsset.getName() + "' → '" + newAsset.getName() + "'");
        }
        if (!Objects.equals(oldAsset.getCategoryId(), newAsset.getCategoryId())) {
            changes.add("카테고리ID: '" + oldAsset.getCategoryId() + "' → '" + newAsset.getCategoryId() + "'");
        }
        if (!Objects.equals(oldAsset.getSerialNumber(), newAsset.getSerialNumber())) {
            changes.add("시리얼번호: '" + oldAsset.getSerialNumber() + "' → '" + newAsset.getSerialNumber() + "'");
        }
        if (!Objects.equals(oldAsset.getStatus(), newAsset.getStatus())) {
            changes.add("상태: '" + oldAsset.getStatus() + "' → '" + newAsset.getStatus() + "'");
        }
        if (!Objects.equals(oldAsset.getLocation(), newAsset.getLocation())) {
            changes.add("위치: '" + oldAsset.getLocation() + "' → '" + newAsset.getLocation() + "'");
        }

        return changes.isEmpty() ? " (변경사항 없음)" : " [변경내역: " + String.join(", ", changes) + "]";
    }
}
