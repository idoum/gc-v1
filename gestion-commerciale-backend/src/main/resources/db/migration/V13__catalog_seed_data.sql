--
-- @path src/main/resources/db/migration/V13__catalog_seed_data.sql
-- @description Données de seed pour le catalogue (catégories et produits de démonstration)
--

-- Mettre à jour les séquences pour le catalogue
INSERT INTO sequences (type, year, current_value, prefix, padding_length) VALUES
('CATEGORY', YEAR(CURDATE()), 0, 'CAT', 3),
('PRODUCT', YEAR(CURDATE()), 0, 'PRD', 4)
ON DUPLICATE KEY UPDATE 
    current_value = VALUES(current_value),
    prefix = VALUES(prefix),
    padding_length = VALUES(padding_length);

-- Catégories racine
INSERT INTO categories (code, name, description, active, sort_order, icon_class, created_by, updated_by) VALUES
('CAT-' + CAST(YEAR(CURDATE()) AS CHAR) + '-001', 'Informatique', 'Matériel et logiciels informatiques', 1, 10, 'bi-laptop', 'system', 'system'),
('CAT-' + CAST(YEAR(CURDATE()) AS CHAR) + '-002', 'Mobilier', 'Mobilier de bureau et équipements', 1, 20, 'bi-house', 'system', 'system'),
('CAT-' + CAST(YEAR(CURDATE()) AS CHAR) + '-003', 'Fournitures', 'Fournitures de bureau et consommables', 1, 30, 'bi-box', 'system', 'system'),
('CAT-' + CAST(YEAR(CURDATE()) AS CHAR) + '-004', 'Services', 'Services et prestations', 1, 40, 'bi-gear', 'system', 'system');

-- Sous-catégories
INSERT INTO categories (code, name, description, parent_id, active, sort_order, icon_class, created_by, updated_by) VALUES
('CAT-' + CAST(YEAR(CURDATE()) AS CHAR) + '-005', 'Ordinateurs', 'PC de bureau et portables', 1, 1, 10, 'bi-pc-display', 'system', 'system'),
('CAT-' + CAST(YEAR(CURDATE()) AS CHAR) + '-006', 'Périphériques', 'Souris, claviers, écrans', 1, 1, 20, 'bi-mouse', 'system', 'system'),
('CAT-' + CAST(YEAR(CURDATE()) AS CHAR) + '-007', 'Réseau', 'Équipements réseau et wifi', 1, 1, 30, 'bi-wifi', 'system', 'system'),
('CAT-' + CAST(YEAR(CURDATE()) AS CHAR) + '-008', 'Bureaux', 'Tables et bureaux', 2, 1, 10, 'bi-table', 'system', 'system'),
('CAT-' + CAST(YEAR(CURDATE()) AS CHAR) + '-009', 'Sièges', 'Chaises et fauteuils', 2, 1, 20, 'bi-chair', 'system', 'system');

-- Produits de démonstration
INSERT INTO products (code, name, description, category_id, active, status, type, reference, sku, unit_price, cost_price, vat_rate, stock_managed, stock_quantity, min_stock_level, unit, created_by, updated_by) VALUES
-- Ordinateurs
('PRD-' + CAST(YEAR(CURDATE()) AS CHAR) + '-0001', 'PC Portable Dell Latitude', 'Ordinateur portable professionnel 15.6" Intel i5', 5, 1, 'AVAILABLE', 'PRODUCT', 'DELL-LAT-15', 'LAPTOP-DELL-001', 899.00, 699.00, 20.00, 1, 15, 5, 'pce', 'system', 'system'),
('PRD-' + CAST(YEAR(CURDATE()) AS CHAR) + '-0002', 'PC Bureau HP EliteDesk', 'PC de bureau compact Intel i7', 5, 1, 'AVAILABLE', 'PRODUCT', 'HP-ELITE-800', 'DESKTOP-HP-001', 1299.00, 999.00, 20.00, 1, 8, 3, 'pce', 'system', 'system'),
-- Périphériques
('PRD-' + CAST(YEAR(CURDATE()) AS CHAR) + '-0003', 'Écran Dell 24 pouces', 'Moniteur LED Full HD 24"', 6, 1, 'AVAILABLE', 'PRODUCT', 'DELL-MON-24', 'MONITOR-DELL-001', 249.00, 189.00, 20.00, 1, 25, 10, 'pce', 'system', 'system'),
('PRD-' + CAST(YEAR(CURDATE()) AS CHAR) + '-0004', 'Clavier sans fil Logitech', 'Clavier wireless ergonomique', 6, 1, 'AVAILABLE', 'PRODUCT', 'LOGI-KB-001', 'KEYBOARD-LOGI-001', 89.00, 59.00, 20.00, 1, 50, 20, 'pce', 'system', 'system'),
('PRD-' + CAST(YEAR(CURDATE()) AS CHAR) + '-0005', 'Souris optique HP', 'Souris optique 3 boutons', 6, 1, 'AVAILABLE', 'PRODUCT', 'HP-MOU-001', 'MOUSE-HP-001', 25.00, 15.00, 20.00, 1, 100, 30, 'pce', 'system', 'system'),
-- Mobilier
('PRD-' + CAST(YEAR(CURDATE()) AS CHAR) + '-0006', 'Bureau réglable 160x80', 'Bureau assis-debout électrique', 8, 1, 'AVAILABLE', 'PRODUCT', 'DESK-ADJ-160', 'DESK-ELEC-001', 699.00, 499.00, 20.00, 1, 5, 2, 'pce', 'system', 'system'),
('PRD-' + CAST(YEAR(CURDATE()) AS CHAR) + '-0007', 'Chaise ergonomique Herman', 'Fauteuil de bureau ergonomique', 9, 1, 'AVAILABLE', 'PRODUCT', 'CHAIR-ERG-001', 'CHAIR-HERMAN-001', 450.00, 320.00, 20.00, 1, 12, 3, 'pce', 'system', 'system'),
-- Fournitures
('PRD-' + CAST(YEAR(CURDATE()) AS CHAR) + '-0008', 'Ramette papier A4', 'Papier blanc 80g/m² - 500 feuilles', 3, 1, 'AVAILABLE', 'PRODUCT', 'PAPER-A4-80', 'PAPER-A4-001', 8.50, 5.20, 20.00, 1, 200, 50, 'pce', 'system', 'system'),
('PRD-' + CAST(YEAR(CURDATE()) AS CHAR) + '-0009', 'Stylos bille bleus', 'Lot de 10 stylos bille bleus', 3, 1, 'AVAILABLE', 'PRODUCT', 'PEN-BLUE-10', 'PEN-BLUE-001', 12.00, 7.50, 20.00, 1, 80, 20, 'lot', 'system', 'system'),
-- Services
('PRD-' + CAST(YEAR(CURDATE()) AS CHAR) + '-0010', 'Installation poste de travail', 'Installation et configuration PC', 4, 1, 'AVAILABLE', 'SERVICE', 'SRV-INSTALL-PC', 'SERVICE-INSTALL-001', 120.00, 80.00, 20.00, 0, 0, 0, 'heure', 'system', 'system');

-- Mettre à jour les compteurs de séquences
UPDATE sequences SET current_value = 9 WHERE type = 'CATEGORY' AND year = YEAR(CURDATE());
UPDATE sequences SET current_value = 10 WHERE type = 'PRODUCT' AND year = YEAR(CURDATE());
