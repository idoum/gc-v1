/*
 * @path src/main/resources/db/migration/V7__parametrage_init.sql
 * @description Création des tables paramétrage : currencies, settings, sequences
 */
CREATE TABLE currencies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE settings (
    key VARCHAR(100) PRIMARY KEY,
    value VARCHAR(255) NOT NULL
);

CREATE TABLE sequences (
    name VARCHAR(100) PRIMARY KEY,
    next_val BIGINT NOT NULL
);
