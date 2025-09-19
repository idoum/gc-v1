-- 
-- @path db/migration/V2__seed_data.sql
-- @description Seed de test initial (roles, permissions, users et liaisons)
INSERT INTO permissions(name,module,action,resource) VALUES
('SECURITY_READ','SECURITY','READ','ALL'),
('CATALOGUE_READ','CATALOGUE','READ','ALL'),
('CRM_READ','CRM','READ','ALL');
INSERT INTO roles(name) VALUES
('ADMIN'),('MANAGER'),('USER');
INSERT INTO users(username,password,email,active,created_at) VALUES
('admin','$2a$10$...','admin@test.com',TRUE,NOW()),
('manager','$2a$10$...','manager@test.com',TRUE,NOW()),
('user','$2a$10$...','user@test.com',TRUE,NOW());
INSERT INTO role_permissions(role_id,permission_id) VALUES
(1,1),(1,2),(1,3),(2,2),(2,3),(3,3);
INSERT INTO user_roles(user_id,role_id) VALUES
(1,1),(2,2),(3,3);
