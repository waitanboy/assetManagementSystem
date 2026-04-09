-- Sample Categories
INSERT INTO category (name) VALUES ('IT Equipment'), ('Office Supplies');

-- Sample Assets
INSERT INTO asset (category_id, name, serial_number, status, location)
VALUES 
(1, 'MacBook Pro M3', 'SN-2024-001', 'AVAILABLE', '7th Floor'),
(1, 'Dell UltraSharp 27"', 'SN-2024-002', 'RENTED', '8th Floor'),
(2, 'Ergonomic Chair', 'SN-CHAIR-001', 'AVAILABLE', 'Lobby');
