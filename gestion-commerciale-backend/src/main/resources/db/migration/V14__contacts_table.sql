--
-- @path src/main/resources/db/migration/V14__contacts_table.sql
-- @description Migration pour la table contacts CRM avec relations clients
--

-- Création de la table contacts
CREATE TABLE contacts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    type ENUM('CONTACT', 'LEAD', 'PROSPECT', 'PARTNER', 'SUPPLIER', 'INTERNAL') NOT NULL DEFAULT 'CONTACT',
    status ENUM('ACTIVE', 'INACTIVE', 'BLOCKED', 'ARCHIVED', 'BOUNCED') NOT NULL DEFAULT 'ACTIVE',
    
    -- Informations personnelles
    civility ENUM('MR', 'MRS', 'MS', 'DR', 'PROF'),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    job_title VARCHAR(100),
    department VARCHAR(100),
    
    -- Informations de contact
    email VARCHAR(150),
    phone VARCHAR(20),
    mobile VARCHAR(20),
    fax VARCHAR(20),
    website VARCHAR(200),
    
    -- Adresse
    address_line1 VARCHAR(200),
    address_line2 VARCHAR(200),
    city VARCHAR(100),
    postal_code VARCHAR(20),
    state VARCHAR(100),
    country VARCHAR(100),
    
    -- Informations complémentaires
    birth_date DATE,
    notes TEXT,
    avatar_url VARCHAR(500),
    
    -- Préférences de communication
    email_opt_in BOOLEAN NOT NULL DEFAULT TRUE,
    sms_opt_in BOOLEAN NOT NULL DEFAULT TRUE,
    phone_opt_in BOOLEAN NOT NULL DEFAULT TRUE,
    preferred_language ENUM('FRENCH', 'ENGLISH', 'SPANISH', 'GERMAN', 'ITALIAN') NOT NULL DEFAULT 'FRENCH',
    preferred_contact ENUM('EMAIL', 'PHONE', 'MOBILE', 'SMS', 'MAIL', 'NONE') NOT NULL DEFAULT 'EMAIL',
    
    -- Relation client
    customer_id BIGINT NOT NULL,
    
    -- Informations de priorité et importance
    priority ENUM('LOW', 'NORMAL', 'HIGH', 'CRITICAL') NOT NULL DEFAULT 'NORMAL',
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    is_decision_maker BOOLEAN NOT NULL DEFAULT FALSE,
    is_influencer BOOLEAN NOT NULL DEFAULT FALSE,
    
    -- Informations de dernière interaction
    last_contact_date TIMESTAMP NULL,
    next_contact_date TIMESTAMP NULL,
    last_contact_note VARCHAR(500),
    
    -- Audit
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    
    -- Contraintes
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    
    -- Index pour les performances
    INDEX idx_contacts_customer_id (customer_id),
    INDEX idx_contacts_email (email),
    INDEX idx_contacts_phone (phone),
    INDEX idx_contacts_mobile (mobile),
    INDEX idx_contacts_status (status),
    INDEX idx_contacts_type (type),
    INDEX idx_contacts_priority (priority),
    INDEX idx_contacts_is_primary (is_primary),
    INDEX idx_contacts_last_contact_date (last_contact_date),
    INDEX idx_contacts_next_contact_date (next_contact_date),
    INDEX idx_contacts_birth_date (birth_date),
    INDEX idx_contacts_name (last_name, first_name),
    
    -- Contraintes d'unicité
    UNIQUE KEY uk_contacts_email (email),
    UNIQUE KEY uk_contacts_code (code)
);

-- Contrainte pour s'assurer qu'il n'y a qu'un seul contact primaire par client
-- Cette contrainte sera vérifiée au niveau applicatif pour plus de flexibilité

-- Ajouter un trigger pour mettre à jour updated_at automatiquement
DELIMITER $$
CREATE TRIGGER contacts_updated_at_trigger
    BEFORE UPDATE ON contacts
    FOR EACH ROW
BEGIN
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END$$
DELIMITER ;
