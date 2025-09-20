/*
 * @path src/main/resources/db/migration/V7__parametrage_init.sql
 * @description Création des tables paramétrage : currencies, settings, sequences
 */
CREATE TABLE IF NOT EXISTS currencies (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(10) NOT NULL,
    name VARCHAR(100) NOT NULL
);
CREATE TABLE IF NOT EXISTS settings (
    `key` VARCHAR(100) PRIMARY KEY,
    `value` VARCHAR(255)
);
CREATE TABLE IF NOT EXISTS sequences (
    `name` VARCHAR(100) PRIMARY KEY,
    next_val INT NOT NULL
);