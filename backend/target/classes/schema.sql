-- DROP TABLE IF EXISTS transaction;
-- DROP TABLE IF EXISTS asset;
-- DROP TABLE IF EXISTS category;
-- DROP TABLE IF EXISTS users;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL DEFAULT 'User',
    role VARCHAR(50) NOT NULL,
    department VARCHAR(100),
    status VARCHAR(50) DEFAULT 'APPROVED'
);

CREATE TABLE IF NOT EXISTS category (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    use_ocr BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS department (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

INSERT IGNORE INTO department (id, name) VALUES (1, 'Engineering');
INSERT IGNORE INTO department (id, name) VALUES (2, 'Human Resources');
INSERT IGNORE INTO department (id, name) VALUES (3, 'Information Technology');
INSERT IGNORE INTO department (id, name) VALUES (4, 'Marketing');
INSERT IGNORE INTO department (id, name) VALUES (5, 'Sales');


CREATE TABLE IF NOT EXISTS asset (
    id INT AUTO_INCREMENT PRIMARY KEY,
    category_id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    serial_number VARCHAR(100) UNIQUE NOT NULL,
    status VARCHAR(50) NOT NULL,
    location VARCHAR(255),
    image_url VARCHAR(500),
    use_yn CHAR(1) DEFAULT 'Y',
    FOREIGN KEY (category_id) REFERENCES category(id)
);

CREATE TABLE IF NOT EXISTS `transaction` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    asset_id INT,
    user_id INT NOT NULL,
    type VARCHAR(50) NOT NULL,
    transaction_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    due_date DATE NULL,
    note TEXT,
    signature_data LONGTEXT, -- Base64 encoded signature image
    ocr_data TEXT, -- Extracted ID card info
    FOREIGN KEY (asset_id) REFERENCES asset(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS rental_request (
    id INT AUTO_INCREMENT PRIMARY KEY,
    asset_id INT NOT NULL,
    user_id INT NOT NULL,
    request_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    planned_due_date DATE NOT NULL,
    purpose TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    processed_by INT,
    process_date TIMESTAMP NULL,
    reject_reason TEXT,
    ocr_data TEXT,
    FOREIGN KEY (asset_id) REFERENCES asset(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (processed_by) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS notice (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    author_id INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    FOREIGN KEY (author_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS repair_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    asset_id INT NOT NULL,
    reported_by INT NOT NULL,
    reason TEXT NOT NULL,
    estimated_cost DECIMAL(10,2),
    final_cost DECIMAL(10,2),
    status VARCHAR(50) NOT NULL DEFAULT 'IN_PROGRESS',
    start_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_date TIMESTAMP NULL,
    FOREIGN KEY (asset_id) REFERENCES asset(id),
    FOREIGN KEY (reported_by) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS chat_message (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sender_id INT NOT NULL,
    receiver_id INT,
    content TEXT NOT NULL,
    timestamp DATETIME NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS board_post (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    author_id INT NOT NULL,
    view_count INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    FOREIGN KEY (author_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS board_comment (
    id INT AUTO_INCREMENT PRIMARY KEY,
    post_id INT NOT NULL,
    author_id INT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    FOREIGN KEY (post_id) REFERENCES board_post(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES users(id)
);
