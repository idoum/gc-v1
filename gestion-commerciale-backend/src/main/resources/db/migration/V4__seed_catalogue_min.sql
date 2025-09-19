/*
 * @path src/main/resources/db/migration/V3__seed_catalogue_min.sql
 * @description Données de référence catalogue : catégories de base, taxes standard
 */
INSERT INTO categories(code) VALUES ('DEFAULT');
INSERT INTO taxes(code, rate) VALUES ('STANDARD', 20.00);
