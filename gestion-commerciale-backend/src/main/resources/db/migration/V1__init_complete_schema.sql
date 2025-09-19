-- 
-- @path src/main/resources/db/migration/V1__init_complete_schema.sql
-- @description Migration unique consolidée avec schéma complet et données de test (version corrigée)
--

-- Supprimer les tables si elles existent (utile en dev)
DROP TABLE IF EXISTS audit_logs;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS role_permissions;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS permissions;

-- ==========================================
-- CRÉATION DES TABLES
-- ==========================================

-- Table des permissions
CREATE TABLE permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    module VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    resource VARCHAR(50),
    INDEX idx_module (module),
    INDEX idx_action (action),
    INDEX idx_resource (resource)
);

-- Table des rôles
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(500)
);

-- Table des utilisateurs
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    failed_attempts INT DEFAULT 0,
    locked_until TIMESTAMP NULL,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_active (active)
);

-- Table de liaison rôle-permission
CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- Table de liaison utilisateur-rôle
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Table des logs d'audit
CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    username VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    module VARCHAR(50) NOT NULL,
    target_type VARCHAR(100),
    target_id VARCHAR(100),
    target_name VARCHAR(255),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent VARCHAR(1000),
    session_id VARCHAR(255),
    status ENUM('SUCCESS', 'FAILURE', 'ERROR') DEFAULT 'SUCCESS',
    error_message VARCHAR(1000),
    old_values TEXT,
    new_values TEXT,
    INDEX idx_username (username),
    INDEX idx_action (action),
    INDEX idx_module (module),
    INDEX idx_timestamp (timestamp),
    INDEX idx_status (status),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- ==========================================
-- DONNÉES DE TEST
-- ==========================================

-- Permissions (dans l'ordre pour éviter les doublons)
INSERT INTO permissions (name, description, module, action, resource) VALUES 
('SECURITY_USER_CREATE', 'Créer un utilisateur', 'SECURITY', 'CREATE', 'USER'),
('SECURITY_USER_READ', 'Consulter les utilisateurs', 'SECURITY', 'READ', 'USER'),
('SECURITY_USER_UPDATE', 'Modifier un utilisateur', 'SECURITY', 'UPDATE', 'USER'),
('SECURITY_USER_DELETE', 'Supprimer un utilisateur', 'SECURITY', 'DELETE', 'USER'),
('SECURITY_ROLE_CREATE', 'Créer un rôle', 'SECURITY', 'CREATE', 'ROLE'),
('SECURITY_ROLE_READ', 'Consulter les rôles', 'SECURITY', 'READ', 'ROLE'),
('SECURITY_ROLE_UPDATE', 'Modifier un rôle', 'SECURITY', 'UPDATE', 'ROLE'),
('SECURITY_ROLE_DELETE', 'Supprimer un rôle', 'SECURITY', 'DELETE', 'ROLE'),
('SECURITY_AUDIT_READ', 'Consulter les logs d\'audit', 'SECURITY', 'READ', 'AUDIT'),
('CATALOGUE_PRODUCT_CREATE', 'Créer un produit', 'CATALOGUE', 'CREATE', 'PRODUCT'),
('CATALOGUE_PRODUCT_READ', 'Consulter les produits', 'CATALOGUE', 'READ', 'PRODUCT'),
('CATALOGUE_PRODUCT_UPDATE', 'Modifier un produit', 'CATALOGUE', 'UPDATE', 'PRODUCT'),
('CATALOGUE_PRODUCT_DELETE', 'Supprimer un produit', 'CATALOGUE', 'DELETE', 'PRODUCT'),
('CRM_CUSTOMER_CREATE', 'Créer un client', 'CRM', 'CREATE', 'CUSTOMER'),
('CRM_CUSTOMER_READ', 'Consulter les clients', 'CRM', 'READ', 'CUSTOMER'),
('CRM_CUSTOMER_UPDATE', 'Modifier un client', 'CRM', 'UPDATE', 'CUSTOMER'),
('CRM_CUSTOMER_DELETE', 'Supprimer un client', 'CRM', 'DELETE', 'CUSTOMER');

-- Rôles
INSERT INTO roles (name, description) VALUES 
('ADMIN', 'Administrateur système - Tous droits'),
('MANAGER', 'Gestionnaire - Gestion opérationnelle'),
('USER', 'Utilisateur standard - Consultation et saisie');

-- Utilisateurs (mot de passe: admin123, manager123, user123)
INSERT INTO users (username, password, email, first_name, last_name, active) VALUES 
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin@test.com', 'Super', 'Admin', TRUE),
('manager', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'manager@test.com', 'Jean', 'Manager', TRUE),
('user', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'user@test.com', 'Marie', 'User', TRUE);

-- Attribution des permissions aux rôles (requêtes corrigées)

-- Rôle ADMIN : TOUTES les permissions
INSERT INTO role_permissions (role_id, permission_id) 
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p WHERE r.name = 'ADMIN';

-- Rôle MANAGER : Permissions spécifiques (requête corrigée avec parenthèses)
INSERT INTO role_permissions (role_id, permission_id) 
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'MANAGER' 
AND (p.name LIKE 'CATALOGUE_%' OR p.name LIKE 'CRM_%' OR p.name = 'SECURITY_AUDIT_READ');

-- Rôle USER : Permissions de base
INSERT INTO role_permissions (role_id, permission_id) 
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'USER' 
AND p.name IN ('CATALOGUE_PRODUCT_READ', 'CRM_CUSTOMER_READ', 'CRM_CUSTOMER_CREATE', 'CRM_CUSTOMER_UPDATE');

-- Attribution des rôles aux utilisateurs
INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'admin' AND r.name = 'ADMIN';

INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'manager' AND r.name = 'MANAGER';

INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'user' AND r.name = 'USER';

-- Logs d'audit de test
INSERT INTO audit_logs (username, action, module, target_type, target_name, ip_address, status) VALUES 
('admin', 'LOGIN', 'SECURITY', 'User', 'admin', '127.0.0.1', 'SUCCESS'),
('manager', 'LOGIN', 'SECURITY', 'User', 'manager', '192.168.1.100', 'SUCCESS'),
('user', 'LOGIN', 'SECURITY', 'User', 'user', '192.168.1.101', 'SUCCESS');
