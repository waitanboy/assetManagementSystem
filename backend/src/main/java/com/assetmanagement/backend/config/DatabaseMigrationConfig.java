package com.assetmanagement.backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DatabaseMigrationConfig {

    private final JdbcTemplate jdbcTemplate;

    @Bean
    public CommandLineRunner migrateDatabase() {
        return args -> {
            log.info("Starting database migration check...");

            addColumnIfMissing("users", "name", "VARCHAR(100) NOT NULL DEFAULT 'User' AFTER password");
            addColumnIfMissing("category", "use_ocr", "BOOLEAN DEFAULT FALSE");
            addColumnIfMissing("rental_request", "ocr_data", "TEXT");
            addColumnIfMissing("transaction", "ocr_data", "TEXT");
            addColumnIfMissing("transaction", "signature_data", "LONGTEXT");

            log.info("Database migration check completed.");
        };
    }

    private void addColumnIfMissing(String tableName, String columnName, String definition) {
        try {
            // Check if column exists
            String checkSql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
                               "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, tableName, columnName);

            if (count != null && count == 0) {
                log.info("Adding column '{}' to table '{}'", columnName, tableName);
                String alterSql = String.format("ALTER TABLE %s ADD COLUMN %s %s", tableName, columnName, definition);
                jdbcTemplate.execute(alterSql);
            } else {
                log.debug("Column '{}' already exists in table '{}'", columnName, tableName);
            }
        } catch (Exception e) {
            log.error("Failed to check or add column '{}' to table '{}': {}", columnName, tableName, e.getMessage());
        }
    }
}
