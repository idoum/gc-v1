-- V1__init.sql
-- Monolithique SANS SEED : uniquement le schéma (tables, index, FKs)
-- Compat: MySQL 8.0+

SET NAMES utf8mb4 COLLATE utf8mb4_0900_ai_ci;
SET time_zone = '+00:00';
SET sql_mode  = 'STRICT_ALL_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- =========================================================
-- 1) SECURITE
-- =========================================================
CREATE TABLE IF NOT EXISTS users (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  username      VARCHAR(100)    NOT NULL,
  email         VARCHAR(190)    NOT NULL,
  password_hash VARCHAR(100)    NOT NULL,
  is_active     TINYINT(1)      NOT NULL DEFAULT 1,
  created_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY ux_users_username (username),
  UNIQUE KEY ux_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS roles (
  id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  code       VARCHAR(50)     NOT NULL,
  name       VARCHAR(120)    NOT NULL,
  created_at TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY ux_roles_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS permissions (
  id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  code       VARCHAR(80)     NOT NULL,
  name       VARCHAR(160)    NOT NULL,
  created_at TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY ux_permissions_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS user_roles (
  user_id    BIGINT UNSIGNED NOT NULL,
  role_id    BIGINT UNSIGNED NOT NULL,
  created_at TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, role_id),
  KEY ix_user_roles_role (role_id),
  CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS role_permissions (
  role_id       BIGINT UNSIGNED NOT NULL,
  permission_id BIGINT UNSIGNED NOT NULL,
  created_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (role_id, permission_id),
  KEY ix_role_permissions_perm (permission_id),
  CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_role_permissions_perm FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================================================
-- 2) OUTILS GENERIQUES
-- =========================================================
CREATE TABLE IF NOT EXISTS sequences (
  name       VARCHAR(100)    NOT NULL,
  next_value BIGINT UNSIGNED NOT NULL,
  updated_at TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS audit_log (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  entity_type  VARCHAR(120)    NOT NULL,
  entity_id    VARCHAR(100)    NOT NULL,
  action       VARCHAR(30)     NOT NULL, -- CREATE/UPDATE/DELETE/LOGIN/...
  details      JSON            NULL,
  created_at   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by   BIGINT UNSIGNED NULL,
  PRIMARY KEY (id),
  KEY ix_audit_entity (entity_type, entity_id),
  KEY ix_audit_created_by (created_by),
  CONSTRAINT fk_audit_user FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================================================
-- 3) CATALOGUE (categories unifiees)
-- =========================================================
CREATE TABLE IF NOT EXISTS categories (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  name        VARCHAR(150)    NOT NULL,
  slug        VARCHAR(150)    NOT NULL,
  description TEXT            NULL,
  parent_id   BIGINT UNSIGNED NULL,
  is_active   TINYINT(1)      NOT NULL DEFAULT 1,
  created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY ux_categories_slug (slug),
  KEY ix_categories_parent (parent_id),
  KEY ix_categories_active (is_active),
  CONSTRAINT fk_categories_parent FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS products (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  name        VARCHAR(200)    NOT NULL,
  sku         VARCHAR(64)     NOT NULL,
  description TEXT            NULL,
  price       DECIMAL(10,2)   NOT NULL DEFAULT 0.00,
  stock_qty   INT             NOT NULL DEFAULT 0,
  is_active   TINYINT(1)      NOT NULL DEFAULT 1,
  created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY ux_products_sku (sku),
  KEY ix_products_active (is_active),
  KEY ix_products_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS product_categories (
  product_id  BIGINT UNSIGNED NOT NULL,
  category_id BIGINT UNSIGNED NOT NULL,
  created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (product_id, category_id),
  KEY ix_pc_category (category_id),
  CONSTRAINT fk_pc_product  FOREIGN KEY (product_id)  REFERENCES products(id)   ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_pc_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Taxes & Price lists (structures minimales ; liaison produit optionnelle a ajouter plus tard si besoin)
CREATE TABLE IF NOT EXISTS taxes (
  id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  code       VARCHAR(20)     NOT NULL,
  rate       DECIMAL(5,2)    NOT NULL,
  created_at TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY ux_taxes_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS price_lists (
  id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  name       VARCHAR(100)    NOT NULL,
  created_at TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY ux_price_lists_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================================================
-- 4) CRM
-- =========================================================
CREATE TABLE IF NOT EXISTS customers (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  code         VARCHAR(32)     NOT NULL,
  company_name VARCHAR(200)    NULL,
  first_name   VARCHAR(120)    NULL,
  last_name    VARCHAR(120)    NULL,
  email        VARCHAR(190)    NULL,
  phone        VARCHAR(50)     NULL,
  is_active    TINYINT(1)      NOT NULL DEFAULT 1,
  created_at   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY ux_customers_code (code),
  UNIQUE KEY ux_customers_email (email),
  KEY ix_customers_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS addresses (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  customer_id  BIGINT UNSIGNED NULL,
  type         VARCHAR(32)     NOT NULL DEFAULT 'PRIMARY', -- PRIMARY/BILLING/SHIPPING/OTHER
  line1        VARCHAR(200)    NOT NULL,
  line2        VARCHAR(200)    NULL,
  city         VARCHAR(120)    NOT NULL,
  state        VARCHAR(120)    NULL,
  postal_code  VARCHAR(30)     NULL,
  country      CHAR(2)         NOT NULL DEFAULT 'FR',
  is_primary   TINYINT(1)      NOT NULL DEFAULT 0,
  created_at   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY ix_addresses_customer (customer_id),
  CONSTRAINT fk_addresses_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS contacts (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  customer_id  BIGINT UNSIGNED NULL,
  first_name   VARCHAR(120)    NOT NULL,
  last_name    VARCHAR(120)    NOT NULL,
  email        VARCHAR(190)    NULL,
  phone        VARCHAR(50)     NULL,
  position     VARCHAR(120)    NULL,
  notes        TEXT            NULL,
  created_at   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY ix_contacts_customer (customer_id),
  UNIQUE KEY ux_contacts_email (email),
  CONSTRAINT fk_contacts_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS contact_categories (
  contact_id  BIGINT UNSIGNED NOT NULL,
  category_id BIGINT UNSIGNED NOT NULL,
  created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (contact_id, category_id),
  KEY ix_cc_category (category_id),
  CONSTRAINT fk_cc_contact  FOREIGN KEY (contact_id)  REFERENCES contacts(id)    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_cc_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================================================
-- 5) VENTES (Orders / Quotes)
-- =========================================================
CREATE TABLE IF NOT EXISTS orders (
  id                   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  code                 VARCHAR(32)     NOT NULL,
  customer_id          BIGINT UNSIGNED NULL,
  billing_address_id   BIGINT UNSIGNED NULL,
  shipping_address_id  BIGINT UNSIGNED NULL,
  status               VARCHAR(32)     NOT NULL DEFAULT 'DRAFT',
  currency_code        CHAR(3)         NOT NULL DEFAULT 'USD',
  subtotal_ht          DECIMAL(14,2)   NOT NULL DEFAULT 0.00,
  tax_amount           DECIMAL(14,2)   NOT NULL DEFAULT 0.00,
  total_ttc            DECIMAL(14,2)   NOT NULL DEFAULT 0.00,
  notes                TEXT            NULL,
  created_at           TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at           TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY ux_orders_code (code),
  KEY ix_orders_customer_id (customer_id),
  KEY ix_orders_billing_address_id (billing_address_id),
  KEY ix_orders_shipping_address_id (shipping_address_id),
  KEY ix_orders_status (status),
  CONSTRAINT fk_orders_customer         FOREIGN KEY (customer_id)         REFERENCES customers(id) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_orders_billing_address  FOREIGN KEY (billing_address_id)  REFERENCES addresses(id) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_orders_shipping_address FOREIGN KEY (shipping_address_id) REFERENCES addresses(id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS order_items (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  order_id        BIGINT UNSIGNED NOT NULL,
  product_id      BIGINT UNSIGNED NULL,
  sku             VARCHAR(64)     NULL,
  name            VARCHAR(255)    NULL,
  quantity        INT             NOT NULL DEFAULT 1,
  unit_price      DECIMAL(14,2)   NOT NULL DEFAULT 0.00,
  tax_rate        DECIMAL(5,2)    NOT NULL DEFAULT 0.00,  -- en %
  line_total_ht   DECIMAL(14,2)   NOT NULL DEFAULT 0.00,
  line_tax_amount DECIMAL(14,2)   NOT NULL DEFAULT 0.00,
  line_total_ttc  DECIMAL(14,2)   NOT NULL DEFAULT 0.00,
  created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY ix_order_items_order_id   (order_id),
  KEY ix_order_items_product_id (product_id),
  CONSTRAINT fk_order_items_order   FOREIGN KEY (order_id)   REFERENCES orders(id)   ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS quotes (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  code          VARCHAR(32)     NOT NULL,
  customer_id   BIGINT UNSIGNED NULL,
  status        VARCHAR(32)     NOT NULL DEFAULT 'DRAFT',
  currency_code CHAR(3)         NOT NULL DEFAULT 'USD',
  subtotal_ht   DECIMAL(14,2)   NOT NULL DEFAULT 0.00,
  tax_amount    DECIMAL(14,2)   NOT NULL DEFAULT 0.00,
  total_ttc     DECIMAL(14,2)   NOT NULL DEFAULT 0.00,
  valid_until   DATE            NULL,
  notes         TEXT            NULL,
  created_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY ux_quotes_code (code),
  KEY ix_quotes_customer_id (customer_id),
  KEY ix_quotes_status (status),
  CONSTRAINT fk_quotes_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS quote_items (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  quote_id        BIGINT UNSIGNED NOT NULL,
  product_id      BIGINT UNSIGNED NULL,
  sku             VARCHAR(64)     NULL,
  name            VARCHAR(255)    NULL,
  quantity        INT             NOT NULL DEFAULT 1,
  unit_price      DECIMAL(14,2)   NOT NULL DEFAULT 0.00,
  tax_rate        DECIMAL(5,2)    NOT NULL DEFAULT 0.00,
  line_total_ht   DECIMAL(14,2)   NOT NULL DEFAULT 0.00,
  line_tax_amount DECIMAL(14,2)   NOT NULL DEFAULT 0.00,
  line_total_ttc  DECIMAL(14,2)   NOT NULL DEFAULT 0.00,
  created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY ix_quote_items_quote_id   (quote_id),
  KEY ix_quote_items_product_id (product_id),
  CONSTRAINT fk_quote_items_quote   FOREIGN KEY (quote_id)   REFERENCES quotes(id)   ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_quote_items_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================================================
-- 6) FACTURATION
-- =========================================================
CREATE TABLE IF NOT EXISTS invoice_status (
  code       VARCHAR(20)  NOT NULL,
  label      VARCHAR(100) NOT NULL,
  is_final   TINYINT(1)   NOT NULL DEFAULT 0,
  created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS invoices (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  code          VARCHAR(32)     NOT NULL,
  order_id      BIGINT UNSIGNED NULL,
  status_code   VARCHAR(20)     NOT NULL,
  issue_date    DATE            NOT NULL,
  due_date      DATE            NULL,
  currency_code CHAR(3)         NOT NULL DEFAULT 'USD',
  subtotal_ht   DECIMAL(14,2)   NOT NULL DEFAULT 0.00,
  tax_amount    DECIMAL(14,2)   NOT NULL DEFAULT 0.00,
  total_ttc     DECIMAL(14,2)   NOT NULL DEFAULT 0.00,
  notes         TEXT            NULL,
  created_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY ux_invoices_code (code),
  KEY ix_invoices_status_code (status_code),
  KEY ix_invoices_order_id (order_id),
  CONSTRAINT fk_invoices_status FOREIGN KEY (status_code) REFERENCES invoice_status(code) ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_invoices_order  FOREIGN KEY (order_id)   REFERENCES orders(id)        ON UPDATE CASCADE ON DELETE SET NULL,
  CHECK (subtotal_ht >= 0 AND tax_amount >= 0 AND total_ttc >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS invoice_items (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  invoice_id      BIGINT UNSIGNED NOT NULL,
  product_id      BIGINT UNSIGNED NULL,
  sku             VARCHAR(64)     NULL,
  name            VARCHAR(255)    NULL,
  quantity        INT             NOT NULL DEFAULT 1,
  unit_price      DECIMAL(14,2)   NOT NULL DEFAULT 0.00,
  tax_rate        DECIMAL(5,2)    NOT NULL DEFAULT 0.00,
  line_total_ht   DECIMAL(14,2)   NOT NULL DEFAULT 0.00,
  line_tax_amount DECIMAL(14,2)   NOT NULL DEFAULT 0.00,
  line_total_ttc  DECIMAL(14,2)   NOT NULL DEFAULT 0.00,
  created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY ix_invoice_items_invoice_id (invoice_id),
  KEY ix_invoice_items_product_id (product_id),
  CONSTRAINT fk_invoice_items_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id)  ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_invoice_items_product FOREIGN KEY (product_id) REFERENCES products(id)  ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================================================
-- 7) I18N
-- =========================================================
CREATE TABLE IF NOT EXISTS product_i18n (
  product_id  BIGINT UNSIGNED NOT NULL,
  locale      VARCHAR(5)      NOT NULL,  -- ex: fr-FR
  name        VARCHAR(255)    NULL,
  description TEXT            NULL,
  PRIMARY KEY (product_id, locale),
  KEY ix_pi18n_product (product_id),
  CONSTRAINT fk_pi18n_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS category_i18n (
  category_id BIGINT UNSIGNED NOT NULL,
  locale      VARCHAR(5)      NOT NULL,
  name        VARCHAR(255)    NULL,
  description TEXT            NULL,
  PRIMARY KEY (category_id, locale),
  KEY ix_ci18n_category (category_id),
  CONSTRAINT fk_ci18n_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
