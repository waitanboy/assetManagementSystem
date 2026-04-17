package com.assetmanagement.backend.controller;

import com.assetmanagement.backend.service.SystemLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/system/logs")
@RequiredArgsConstructor
public class SystemLogController {

    private final SystemLogService logService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<String>> getLogs(@RequestParam(defaultValue = "100") int lines) {
        return ResponseEntity.ok(logService.getRecentLogs(lines));
    }
}
