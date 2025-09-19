-- 
-- @path src/main/resources/data.sql
-- @description Données initiales de test simples
--

-- Données utilisateurs (admin/admin123, manager/manager123, user/user123)
INSERT INTO users (username, password, email, first_name, last_name, active, created_at) VALUES 
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin@test.com', 'Super', 'Admin', TRUE, NOW()),
('manager', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'manager@test.com', 'Jean', 'Manager', TRUE, NOW()),
('user', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'user@test.com', 'Marie', 'User', TRUE, NOW());

-- Rôles de base
INSERT INTO roles (name, description) VALUES 
('ADMIN', 'Administrateur système'),
('MANAGER', 'Gestionnaire'),
('USER', 'Utilisateur standard');

-- Permissions de base
INSERT INTO permissions (name, description, module, action, resource) VALUES 
('SECURITY_READ', 'Consulter la sécurité', 'SECURITY', 'READ', 'ALL'),
('CATALOGUE_READ', 'Consulter les produits', 'CATALOGUE', 'READ', 'ALL'),
('CRM_READ', 'Consulter les clients', 'CRM', 'READ', 'ALL');

-- Attribution des rôles aux utilisateurs
INSERT INTO user_roles (user_id, role_id) VALUES 
(1, 1), -- admin -> ADMIN
(2, 2), -- manager -> MANAGER  
(3, 3); -- user -> USER

-- Attribution des permissions aux rôles
INSERT INTO role_permissions (role_id, permission_id) VALUES 
(1, 1), (1, 2), (1, 3), -- ADMIN a toutes les permissions
(2, 2), (2, 3),          -- MANAGER a catalogue et CRM
(3, 3);                  -- USER a CRM seulement

-- Logs d'audit de test
INSERT INTO audit_logs (username, action, module, target_type, target_name, ip_address, status) VALUES 
('admin', 'LOGIN', 'SECURITY', 'User', 'admin', '127.0.0.1', 'SUCCESS'),
('manager', 'LOGIN', 'SECURITY', 'User', 'manager', '192.168.1.100', 'SUCCESS'),
('user', 'LOGIN', 'SECURITY', 'User', 'user', '192.168.1.101', 'SUCCESS');
