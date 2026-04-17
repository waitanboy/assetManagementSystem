package com.assetmanagement.backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DatabaseMigrationConfig {

    private final JdbcTemplate jdbcTemplate;

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public CommandLineRunner migrateDatabase() {
        return args -> {
            log.info("Starting database migration check...");

            addColumnIfMissing("users", "name", "VARCHAR(100) NOT NULL DEFAULT 'User' AFTER password");
            addColumnIfMissing("category", "use_ocr", "BOOLEAN DEFAULT FALSE");
            addColumnIfMissing("rental_request", "ocr_data", "TEXT");
            addColumnIfMissing("transaction", "ocr_data", "TEXT");
            addColumnIfMissing("transaction", "signature_data", "LONGTEXT");

            addColumnIfMissing("users", "reset_token", "VARCHAR(255)");
            addColumnIfMissing("users", "reset_token_expiry", "TIMESTAMP NULL");
            
            createChatMessageTable();

            log.info("Database migration check completed.");
        };
    }

    private void createChatMessageTable() {
        try {
            // Check if table already exists first
            String checkSql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES " +
                              "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'chat_message'";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            if (count != null && count > 0) {
                log.debug("Table 'chat_message' already exists, skipping creation.");
                return;
            }

            // Create without FK constraints to avoid column type mismatch issues
            String sql = "CREATE TABLE chat_message (" +
                         "id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "sender_id INT NOT NULL, " +
                         "receiver_id INT, " +
                         "content TEXT NOT NULL, " +
                         "timestamp DATETIME NOT NULL, " +
                         "is_read BOOLEAN NOT NULL DEFAULT FALSE" +
                         ")";
            jdbcTemplate.execute(sql);
            log.info("Table 'chat_message' created successfully.");
        } catch (Exception e) {
            log.error("Failed to create 'chat_message' table: {}", e.getMessage());
        }
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
