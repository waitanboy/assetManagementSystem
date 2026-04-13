package com.assetmanagement.backend.controller;

import com.assetmanagement.backend.service.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;

    @PostMapping("/generate-notice")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> generateNotice(@RequestBody Map<String, String> request) {
        String topic = request.get("topic");
        if (topic == null || topic.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Topic is required"));
        }
        
        try {
            String generatedText = aiService.generateNotice(topic);
            return ResponseEntity.ok(Map.of("result", generatedText));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "AI Server error"));
        }
    }

    @PostMapping("/ocr")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> processIDCard(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }
        
        try {
            String extractedInfo = aiService.extractIdCardInfo(file.getBytes());
            return ResponseEntity.ok(Map.of("result", extractedInfo));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "OCR failed: " + e.getMessage()));
        }
    }
}
