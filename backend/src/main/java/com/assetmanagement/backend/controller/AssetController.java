package com.assetmanagement.backend.controller;

import com.assetmanagement.backend.entity.Asset;
import com.assetmanagement.backend.dto.TransactionRequest;
import com.assetmanagement.backend.service.AssetService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    @GetMapping
    public ResponseEntity<List<Asset>> getAllAssets(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId) {
        return ResponseEntity.ok(assetService.getAllAssets(search, status, categoryId));
    }

    @GetMapping("/my")
    public ResponseEntity<List<Asset>> getMyRentedAssets() {
        return ResponseEntity.ok(assetService.getMyRentedAssets());
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Asset>> getAssetsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(assetService.getMyRentedAssets(userId));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getAssetStats() {
        return ResponseEntity.ok(assetService.getAssetStats());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Asset> getAssetById(@PathVariable Long id) {
        Asset asset = assetService.getAssetById(id);
        if (asset != null) {
            return ResponseEntity.ok(asset);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/repairs/all")
    public ResponseEntity<List<com.assetmanagement.backend.entity.RepairLog>> getAllRepairs() {
        return ResponseEntity.ok(assetService.getAllRepairs());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Asset> createAsset(@Valid @RequestBody Asset asset) {
        return new ResponseEntity<>(assetService.createAsset(asset), HttpStatus.CREATED);
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> createAssetsBulk(@RequestBody List<Asset> assets) {
        assetService.createAssetsBulk(assets);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateAsset(@PathVariable Long id, @Valid @RequestBody Asset asset) {
        assetService.updateAsset(id, asset);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAsset(@PathVariable Long id) {
        assetService.deleteAsset(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/rent")
    public ResponseEntity<Void> rentAsset(@PathVariable Long id, @Valid @RequestBody TransactionRequest request) {
        assetService.rentAsset(id, request.getUserId(), request.getNote(), request.getDueDate(), request.getSignatureData(), request.getOcrData());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<Void> returnAsset(@PathVariable Long id, @Valid @RequestBody TransactionRequest request) {
        assetService.returnAsset(id, request.getUserId(), request.getNote(), request.getSignatureData());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/reassign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> reassignRenter(@PathVariable Long id, @RequestBody TransactionRequest request) {
        assetService.reassignRenter(id, request.getUserId(), request.getDueDate());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/repair/start")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> startRepair(@PathVariable Long id, @RequestBody com.assetmanagement.backend.dto.RepairRequest request) {
        assetService.startRepair(id, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/repair/complete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> completeRepair(@PathVariable Long id, @RequestBody com.assetmanagement.backend.dto.RepairRequest request) {
        assetService.completeRepair(id, request);
        return ResponseEntity.ok().build();
    }
}
