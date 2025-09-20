--
-- @path src/main/resources/db/migration/V15__contacts_seed_data.sql
-- @description Données de seed pour les contacts CRM avec relations clients existants
--

-- Mettre à jour les séquences pour les contacts
INSERT INTO sequences (type, year, current_value, prefix, padding_length) VALUES
('CONTACT', YEAR(CURDATE()), 0, 'CNT', 4)
ON DUPLICATE KEY UPDATE 
    current_value = VALUES(current_value),
    prefix = VALUES(prefix),
    padding_length = VALUES(padding_length);

-- Contacts pour les clients existants (supposons que nous avons des clients avec ID 1-5)
-- Contact principal pour TechCorp Solutions (client ID 1)
INSERT INTO contacts (code, type, status, civility, first_name, last_name, job_title, department, email, phone, mobile, customer_id, priority, is_primary, is_decision_maker, created_by, updated_by) VALUES
('CNT-' + CAST(YEAR(CURDATE()) AS CHAR) + '-0001', 'CONTACT', 'ACTIVE', 'MR', 'Jean', 'Martin', 'Directeur IT', 'Informatique', 'j.martin@techcorp.com', '+33 1 23 45 67 89', '+33 6 12 34 56 78', 1, 'HIGH', TRUE, TRUE, 'system', 'system');

-- Contact secondaire pour TechCorp Solutions
INSERT INTO contacts (code, type, status, civility, first_name, last_name, job_title, department, email, phone, customer_id, priority, is_primary, is_decision_maker, created_by, updated_by) VALUES
('CNT-' + CAST(YEAR(CURDATE()) AS CHAR) + '-0002', 'CONTACT', 'ACTIVE', 'MRS', 'Marie', 'Dubois', 'Chef de projet', 'Informatique', 'm.dubois@techcorp.com', '+33 1 23 45 67 90', 1, 'NORMAL', FALSE, FALSE, 'system', 'system');

-- Contact pour Innovate SARL (client ID 2)
INSERT INTO contacts (code, type, status, civility, first_name, last_name, job_title, department, email, mobile, customer_id, priority, is_primary, is_decision_maker, is_influencer, created_by, updated_by) VALUES
('CNT-' + CAST(YEAR(CURDATE()) AS CHAR) + '-0003', 'CONTACT', 'ACTIVE', 'MR', 'Pierre', 'Lefebvre', 'PDG', 'Direction', 'p.lefebvre@innovate-sarl.com', '+33 6 98 76 54 32', 2, 'CRITICAL', TRUE, TRUE, TRUE, 'system', 'system');

-- Contact assistant pour Innovate SARL
INSERT INTO contacts (code, type, status, civility, first_name, last_name, job_title, department, email, phone, customer_id, priority, is_primary, created_by, updated_by) VALUES
('CNT-' + CAST(YEAR(CURDATE()) AS CHAR) + '-0004', 'CONTACT', 'ACTIVE', 'MS', 'Sophie', 'Bernard', 'Assistante de direction', 'Direction', 's.bernard@innovate-sarl.com', '+33 1 34 56 78 90', 2, 'NORMAL', FALSE, 'system', 'system');

-- Contact pour Global Enterprises (client ID 3)
INSERT INTO contacts (code, type, status, civility, first_name, last_name, job_title, department, email, phone, mobile, customer_id, priority, is_primary, is_decision_maker, birth_date, created_by, updated_by) VALUES
('CNT-' + CAST(YEAR(CURDATE()) AS CHAR) + '-0005', 'CONTACT', 'ACTIVE', 'DR', 'Antoine', 'Moreau', 'Directeur Technique', 'R&D', 'a.moreau@global-ent.com', '+33 1 45 67 89 12', '+33 6 11 22 33 44', 3, 'HIGH', TRUE, TRUE, '1975-03-15', 'system', 'system');

-- Prospect (LEAD) non encore client
INSERT INTO contacts (code, type, status, civility, first_name, last_name, job_title, email, phone, customer_id, priority, is_primary, last_contact_date, next_contact_date, last_contact_note, created_by, updated_by) VALUES
('CNT-' + CAST(YEAR(CURDATE()) AS CHAR) + '-0006', 'LEAD', 'ACTIVE', 'MRS', 'Isabelle', 'Roux', 'Directrice Achats', 'i.roux@prospect.com', '+33 1 56 78 90 12', 4, 'HIGH', TRUE, '2025-09-15 14:30:00', '2025-09-25 10:00:00', 'Premier contact téléphonique très positif, intéressée par notre offre', 'system', 'system');

-- Contact avec adresse différente
INSERT INTO contacts (code, type, status, civility, first_name, last_name, job_title, department, email, phone, address_line1, address_line2, city, postal_code, country, customer_id, priority, is_primary, created_by, updated_by) VALUES
('CNT-' + CAST(YEAR(CURDATE()) AS CHAR) + '-0007', 'CONTACT', 'ACTIVE', 'MR', 'Fabien', 'Petit', 'Responsable Logistique', 'Supply Chain', 'f.petit@startup-inno.com', '+33 1 67 89 01 23', '456 Avenue des Entrepreneurs', 'Bât. C, Bureau 201', 'Lyon', '69000', 'France', 5, 'NORMAL', TRUE, 'system', 'system');

-- Contact partenaire
INSERT INTO contacts (code, type, status, civility, first_name, last_name, job_title, email, mobile, customer_id, priority, is_primary, is_influencer, preferred_contact, created_by, updated_by) VALUES
('CNT-' + CAST(YEAR(CURDATE()) AS CHAR) + '-0008', 'PARTNER', 'ACTIVE', 'MRS', 'Céline', 'Garcia', 'Business Developer', 'c.garcia@partner.com', '+33 6 55 44 33 22', 6, 'NORMAL', TRUE, TRUE, 'MOBILE', 'system', 'system');

-- Contact avec anniversaire aujourd'hui (pour tester les alertes)
INSERT INTO contacts (code, type, status, civility, first_name, last_name, job_title, email, customer_id, priority, is_primary, birth_date, created_by, updated_by) VALUES
('CNT-' + CAST(YEAR(CURDATE()) AS CHAR) + '-0009', 'CONTACT', 'ACTIVE', 'MR', 'Thomas', 'Leroy', 'Acheteur Senior', 't.leroy@client.com', 7, 'NORMAL', TRUE, CONCAT(YEAR(CURDATE()) - 35, '-', LPAD(MONTH(CURDATE()), 2, '0'), '-', LPAD(DAY(CURDATE()), 2, '0')), 'system', 'system');

-- Contact en retard de suivi (overdue)
INSERT INTO contacts (code, type, status, civility, first_name, last_name, job_title, email, customer_id, priority, is_primary, last_contact_date, next_contact_date, last_contact_note, created_by, updated_by) VALUES
('CNT-' + CAST(YEAR(CURDATE()) AS CHAR) + '-0010', 'LEAD', 'ACTIVE', 'MRS', 'Nathalie', 'Blanc', 'DRH', 'n.blanc@retard.com', 8, 'HIGH', TRUE, '2025-09-10 16:00:00', '2025-09-18 09:00:00', 'En attente de réponse depuis notre proposition', 'system', 'system');

-- Mettre à jour le compteur de séquences
UPDATE sequences SET current_value = 10 WHERE type = 'CONTACT' AND year = YEAR(CURDATE());

-- Ajouter quelques notes et interactions supplémentaires
UPDATE contacts SET notes = 'Contact très technique, préfère les discussions approfondies. Disponible de préférence l\'après-midi.' WHERE code = 'CNT-' + CAST(YEAR(CURDATE()) AS CHAR) + '-0001';
UPDATE contacts SET notes = 'Très organisée, envoie toujours un compte-rendu après les réunions. Point de contact idéal pour le suivi projet.' WHERE code = 'CNT-' + CAST(YEAR(CURDATE()) AS CHAR) + '-0002';
UPDATE contacts SET notes = 'Décideur final, mais prend le temps de consulter son équipe. Préparer des présentations détaillées.' WHERE code = 'CNT-' + CAST(YEAR(CURDATE()) AS CHAR) + '-0003';
UPDATE contacts SET notes = 'Gère l\'agenda du PDG. Passer par elle pour planifier les rendez-vous importants.' WHERE code = 'CNT-' + CAST(YEAR(CURDATE()) AS CHAR) + '-0004';
UPDATE contacts SET notes = 'Expert technique, très exigeant sur les spécifications. Préparer des démonstrations concrètes.' WHERE code = 'CNT-' + CAST(YEAR(CURDATE()) AS CHAR) + '-0005';
