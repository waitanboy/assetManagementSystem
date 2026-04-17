package com.assetmanagement.backend.controller;

import com.assetmanagement.backend.entity.Transaction;
import com.assetmanagement.backend.mapper.TransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionMapper transactionMapper;

    @GetMapping("/recent")
    public ResponseEntity<List<Transaction>> getRecentTransactions(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(transactionMapper.findRecent(limit));
    }

    @GetMapping("/asset/{assetId}")
    public ResponseEntity<List<Transaction>> getHistoryByAssetId(@PathVariable Long assetId) {
        return ResponseEntity.ok(transactionMapper.findByAssetId(assetId));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Transaction>> getHistoryByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(transactionMapper.findByUserId(userId));
    }
}
