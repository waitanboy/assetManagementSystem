package com.assetmanagement.backend.controller;

import com.assetmanagement.backend.entity.RentalRequest;
import com.assetmanagement.backend.service.RentalRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class RentalRequestController {

    private final RentalRequestService rentalRequestService;

    @PostMapping
    public ResponseEntity<Void> createRequest(@RequestBody RentalRequest request) {
        rentalRequestService.createRequest(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RentalRequest>> getPendingRequests() {
        return ResponseEntity.ok(rentalRequestService.getPendingRequests());
    }

    @GetMapping("/my")
    public ResponseEntity<List<RentalRequest>> getMyRequests() {
        return ResponseEntity.ok(rentalRequestService.getMyRequests());
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RentalRequest>> getAllRequests() {
        return ResponseEntity.ok(rentalRequestService.getAllRequests());
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> approveRequest(@PathVariable Long id, @RequestBody Map<String, String> body) {
        rentalRequestService.approveRequest(id, body.get("signatureData"), body.get("ocrData"));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> rejectRequest(@PathVariable Long id, @RequestBody Map<String, String> body) {
        rentalRequestService.rejectRequest(id, body.get("reason"));
        return ResponseEntity.ok().build();
    }
}
