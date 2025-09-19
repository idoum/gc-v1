/*
 * @path src/main/resources/db/migration/V8__i18n_init.sql
 * @description Création des tables _i18n pour le contenu multilingue
 */
CREATE TABLE product_i18n (
    product_id BIGINT NOT NULL,
    locale VARCHAR(5) NOT NULL,
    name VARCHAR(255),
    description TEXT,
    PRIMARY KEY(product_id, locale),
    CONSTRAINT fk_pi18n_product FOREIGN KEY(product_id) REFERENCES products(id)
);

CREATE TABLE category_i18n (
    category_id BIGINT NOT NULL,
    locale VARCHAR(5) NOT NULL,
    name VARCHAR(255),
    description TEXT,
    PRIMARY KEY(category_id, locale),
    CONSTRAINT fk_ci18n_category FOREIGN KEY(category_id) REFERENCES categories(id)
);
