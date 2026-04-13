package com.assetmanagement.backend.service;

import com.assetmanagement.backend.entity.Asset;
import com.assetmanagement.backend.entity.RentalRequest;
import com.assetmanagement.backend.mapper.AssetMapper;
import com.assetmanagement.backend.mapper.RentalRequestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RentalRequestService {

    private final RentalRequestMapper rentalRequestMapper;
    private final AssetMapper assetMapper;
    private final AssetService assetService;
    private final UserService userService;

    @Transactional
    public void createRequest(RentalRequest request) {
        Long currentUserId = userService.getCurrentUserId();
        request.setUserId(currentUserId);
        request.setRequestDate(LocalDateTime.now());
        request.setStatus("PENDING");
        
        // Update asset status to REQUESTED
        Asset asset = assetMapper.findById(request.getAssetId());
        if (asset == null) throw new RuntimeException("Asset not found");
        if (!"AVAILABLE".equals(asset.getStatus())) {
            throw new RuntimeException("Asset is not available for request");
        }
        
        asset.setStatus("REQUESTED");
        assetMapper.update(asset);
        
        rentalRequestMapper.insert(request);
    }

    @Transactional(readOnly = true)
    public List<RentalRequest> getPendingRequests() {
        return rentalRequestMapper.findByStatus("PENDING");
    }

    @Transactional(readOnly = true)
    public List<RentalRequest> getMyRequests() {
        Long currentUserId = userService.getCurrentUserId();
        return rentalRequestMapper.findByUserId(currentUserId);
    }

    @Transactional(readOnly = true)
    public List<RentalRequest> getAllRequests() {
        return rentalRequestMapper.findAll();
    }

    @Transactional
    public void approveRequest(Long requestId, String signatureData, String ocrData) {
        RentalRequest request = rentalRequestMapper.findById(requestId);
        if (request == null) throw new RuntimeException("Request not found");
        if (!"PENDING".equals(request.getStatus())) {
            throw new RuntimeException("Request is already processed");
        }

        Long adminId = userService.getCurrentUserId();
        request.setStatus("APPROVED");
        request.setProcessedBy(adminId);
        request.setProcessDate(LocalDateTime.now());
        request.setOcrData(ocrData);
        rentalRequestMapper.updateStatus(request);

        // Execute actual rental
        assetService.rentAsset(request.getAssetId(), request.getUserId(), request.getPurpose(), request.getPlannedDueDate(), signatureData, ocrData);
    }

    @Transactional
    public void rejectRequest(Long requestId, String reason) {
        RentalRequest request = rentalRequestMapper.findById(requestId);
        if (request == null) throw new RuntimeException("Request not found");
        if (!"PENDING".equals(request.getStatus())) {
            throw new RuntimeException("Request is already processed");
        }

        Long adminId = userService.getCurrentUserId();
        request.setStatus("REJECTED");
        request.setProcessedBy(adminId);
        request.setProcessDate(LocalDateTime.now());
        request.setRejectReason(reason);
        rentalRequestMapper.updateStatus(request);

        // Revert asset status to AVAILABLE
        Asset asset = assetMapper.findById(request.getAssetId());
        if (asset != null) {
            asset.setStatus("AVAILABLE");
            assetMapper.update(asset);
        }
    }
}
