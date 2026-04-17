package com.assetmanagement.backend;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(0) // Run before DataInitializer
@RequiredArgsConstructor
public class TableCreator implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        log.info("Checking and creating missing tables...");
        
        try {
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS board_post (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "title VARCHAR(255) NOT NULL, " +
                    "content TEXT NOT NULL, " +
                    "author_id INT NOT NULL, " +
                    "view_count INT DEFAULT 0, " +
                    "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP NULL, " +
                    "FOREIGN KEY (author_id) REFERENCES users(id)" +
                    ")");
            log.info("Table 'board_post' check/create done.");

            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS board_comment (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "post_id INT NOT NULL, " +
                    "author_id INT NOT NULL, " +
                    "content TEXT NOT NULL, " +
                    "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP NULL, " +
                    "FOREIGN KEY (post_id) REFERENCES board_post(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (author_id) REFERENCES users(id)" +
                    ")");
            log.info("Table 'board_comment' check/create done.");
            
        } catch (Exception e) {
            log.error("Failed to create tables", e);
        }
    }
}
