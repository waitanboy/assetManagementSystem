-- DROP TABLE IF EXISTS transaction;
-- DROP TABLE IF EXISTS asset;
-- DROP TABLE IF EXISTS category;
-- DROP TABLE IF EXISTS users;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    department VARCHAR(100),
    status VARCHAR(50) DEFAULT 'APPROVED'
);

CREATE TABLE IF NOT EXISTS category (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

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
    FOREIGN KEY (asset_id) REFERENCES asset(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
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
