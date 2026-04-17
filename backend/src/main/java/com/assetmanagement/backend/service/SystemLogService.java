package com.assetmanagement.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class SystemLogService {

    @Value("${logging.file.name:logs/app.log}")
    private String logFilePath;

    public List<String> getRecentLogs(int lines) {
        File file = new File(logFilePath);
        if (!file.exists()) {
            return List.of("Log file not found at: " + file.getAbsolutePath());
        }

        try {
            // Read all bytes and convert to String using UTF-8 (lenient mode)
            // This avoids MalformedInputException by replacing invalid characters with '?'
            byte[] bytes = Files.readAllBytes(Paths.get(logFilePath));
            String content = new String(bytes, StandardCharsets.UTF_8);
            String[] allLines = content.split("\\r?\\n");

            int size = allLines.length;
            int start = Math.max(0, size - lines);
            List<String> result = new ArrayList<>();
            for (int i = start; i < size; i++) {
                result.add(allLines[i]);
            }
            return result;
        } catch (IOException e) {
            return List.of("Error reading logs: " + e.getMessage());
        }
    }

    public String getLogFilePath() {
        return new File(logFilePath).getAbsolutePath();
    }
}
