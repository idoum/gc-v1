/*
 * @path src/main/resources/db/migration/V4__crm_init.sql
 * @description Création des tables CRM : customers, addresses
 */
CREATE TABLE customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE addresses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    street VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20),
    country VARCHAR(100),
    CONSTRAINT fk_address_customer FOREIGN KEY(customer_id) REFERENCES customers(id)
);
