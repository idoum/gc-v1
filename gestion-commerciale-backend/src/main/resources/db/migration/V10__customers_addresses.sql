--
-- @path src/main/resources/db/migration/V10__customers_addresses.sql
-- @description Migration pour les tables customers et addresses
--

-- Table customers
CREATE TABLE customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    company_name VARCHAR(150) NOT NULL,
    contact_first_name VARCHAR(100),
    contact_last_name VARCHAR(100),
    email VARCHAR(150) UNIQUE,
    phone VARCHAR(20),
    mobile VARCHAR(20),
    siret VARCHAR(20) UNIQUE,
    vat_number VARCHAR(15) UNIQUE,
    credit_limit DECIMAL(10,2),
    payment_term_days INT NOT NULL DEFAULT 30,
    status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED', 'ARCHIVED') NOT NULL DEFAULT 'ACTIVE',
    type ENUM('COMPANY', 'INDIVIDUAL', 'ADMINISTRATION') NOT NULL DEFAULT 'COMPANY',
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table addresses
CREATE TABLE addresses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    type ENUM('BILLING', 'SHIPPING', 'BOTH', 'OTHER') NOT NULL,
    label VARCHAR(150),
    street1 VARCHAR(150) NOT NULL,
    street2 VARCHAR(150),
    zip_code VARCHAR(10) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100),
    country_code CHAR(2) NOT NULL DEFAULT 'FR',
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    contact_name VARCHAR(100),
    contact_phone VARCHAR(20),
    contact_email VARCHAR(150),
    delivery_instructions TEXT,
    
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table sequences
CREATE TABLE IF NOT EXISTS sequences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    year INT NOT NULL,
    current_value BIGINT NOT NULL DEFAULT 0,
    prefix VARCHAR(10),
    padding_length INT NOT NULL DEFAULT 4,
    
    UNIQUE KEY uk_sequence_type_year (type, year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
