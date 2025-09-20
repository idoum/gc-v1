--
-- @path src/main/resources/db/migration/V11__seed_customers_sequences.sql  
-- @description Données de seed pour les séquences et clients de test
--

-- Initialisation des séquences
INSERT INTO sequences (type, year, current_value, prefix, padding_length) VALUES
('CUSTOMER', YEAR(CURDATE()), 0, 'CLI', 4),
('INVOICE', YEAR(CURDATE()), 0, 'INV', 4),
('ORDER', YEAR(CURDATE()), 0, 'ORD', 4),
('QUOTE', YEAR(CURDATE()), 0, 'QUO', 4),
('PRODUCT', YEAR(CURDATE()), 0, 'PRD', 4);

-- Client de test
INSERT INTO customers (
    code, company_name, contact_first_name, contact_last_name, 
    email, phone, type, status, payment_term_days,
    created_by, updated_by
) VALUES (
    CONCAT('CLI-', YEAR(CURDATE()), '-0001'), 
    'Entreprise Test',
    'Jean', 'Dupont',
    'contact@test.fr',
    '01.23.45.67.89',
    'COMPANY',
    'ACTIVE',
    30,
    'system',
    'system'
);

-- Adresse pour le client de test
INSERT INTO addresses (
    customer_id, type, label, street1, zip_code, city, 
    country_code, is_default, active
) VALUES (
    1, 'BOTH', 'Siège social', '123 Rue de Test', '75001', 'Paris', 'FR', TRUE, TRUE
);

-- Mise à jour des séquences
UPDATE sequences 
SET current_value = 1 
WHERE type = 'CUSTOMER' AND year = YEAR(CURDATE());
