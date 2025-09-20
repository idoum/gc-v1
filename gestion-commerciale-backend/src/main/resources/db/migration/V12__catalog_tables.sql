--
-- @path src/main/resources/db/migration/V12__catalog_tables.sql
-- @description Migration pour les tables de catalogue (catégories et produits)
--

-- Mise à jour de la table categories existante si nécessaire
ALTER TABLE categories 
ADD COLUMN IF NOT EXISTS code VARCHAR(20) UNIQUE AFTER id,
ADD COLUMN IF NOT EXISTS sort_order INT NOT NULL DEFAULT 0 AFTER active,
ADD COLUMN IF NOT EXISTS image_url VARCHAR(255) AFTER sort_order,
ADD COLUMN IF NOT EXISTS icon_class VARCHAR(255) AFTER image_url,
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER icon_class,
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at,
ADD COLUMN IF NOT EXISTS created_by VARCHAR(100) AFTER updated_at,
ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100) AFTER created_by;

-- Mise à jour de la table products existante
ALTER TABLE products
MODIFY COLUMN code VARCHAR(30) NOT NULL UNIQUE,
ADD COLUMN IF NOT EXISTS reference VARCHAR(50) AFTER type,
ADD COLUMN IF NOT EXISTS sku VARCHAR(50) AFTER reference,
ADD COLUMN IF NOT EXISTS ean VARCHAR(20) AFTER sku,
ADD COLUMN IF NOT EXISTS cost_price DECIMAL(10,2) DEFAULT 0.00 AFTER unit_price,
ADD COLUMN IF NOT EXISTS vat_rate DECIMAL(5,2) NOT NULL DEFAULT 20.00 AFTER cost_price,
ADD COLUMN IF NOT EXISTS stock_managed BOOLEAN NOT NULL DEFAULT FALSE AFTER vat_rate,
ADD COLUMN IF NOT EXISTS stock_quantity INT NOT NULL DEFAULT 0 AFTER stock_managed,
ADD COLUMN IF NOT EXISTS min_stock_level INT NOT NULL DEFAULT 0 AFTER stock_quantity,
ADD COLUMN IF NOT EXISTS max_stock_level INT NOT NULL DEFAULT 0 AFTER min_stock_level,
ADD COLUMN IF NOT EXISTS unit VARCHAR(10) DEFAULT 'pce' AFTER max_stock_level,
ADD COLUMN IF NOT EXISTS weight DECIMAL(8,3) AFTER unit,
ADD COLUMN IF NOT EXISTS weight_unit VARCHAR(10) DEFAULT 'kg' AFTER weight,
ADD COLUMN IF NOT EXISTS length DECIMAL(8,2) AFTER weight_unit,
ADD COLUMN IF NOT EXISTS width DECIMAL(8,2) AFTER length,
ADD COLUMN IF NOT EXISTS height DECIMAL(8,2) AFTER width,
ADD COLUMN IF NOT EXISTS dimension_unit VARCHAR(10) DEFAULT 'cm' AFTER height,
ADD COLUMN IF NOT EXISTS image_url VARCHAR(500) AFTER dimension_unit,
ADD COLUMN IF NOT EXISTS thumbnail_url VARCHAR(500) AFTER image_url,
ADD COLUMN IF NOT EXISTS document_url VARCHAR(500) AFTER thumbnail_url,
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER document_url,
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at,
ADD COLUMN IF NOT EXISTS created_by VARCHAR(100) AFTER updated_at,
ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100) AFTER created_by;

-- Modifier les colonnes existantes si nécessaire
ALTER TABLE products 
MODIFY COLUMN status ENUM('AVAILABLE', 'OUT_OF_STOCK', 'DISCONTINUED', 'PENDING', 'DRAFT') NOT NULL DEFAULT 'AVAILABLE',
MODIFY COLUMN type ENUM('PRODUCT', 'SERVICE', 'VARIANT', 'BUNDLE') NOT NULL DEFAULT 'PRODUCT',
MODIFY COLUMN description TEXT;

-- Ajouter les index pour les performances
CREATE INDEX IF NOT EXISTS idx_categories_parent_id ON categories(parent_id);
CREATE INDEX IF NOT EXISTS idx_categories_active ON categories(active);
CREATE INDEX IF NOT EXISTS idx_categories_sort_order ON categories(sort_order);

CREATE INDEX IF NOT EXISTS idx_products_category_id ON products(category_id);
CREATE INDEX IF NOT EXISTS idx_products_active ON products(active);
CREATE INDEX IF NOT EXISTS idx_products_status ON products(status);
CREATE INDEX IF NOT EXISTS idx_products_type ON products(type);
CREATE INDEX IF NOT EXISTS idx_products_sku ON products(sku);
CREATE INDEX IF NOT EXISTS idx_products_ean ON products(ean);
CREATE INDEX IF NOT EXISTS idx_products_stock_managed ON products(stock_managed);
CREATE INDEX IF NOT EXISTS idx_products_stock_quantity ON products(stock_quantity);

-- Contraintes d'unicité supplémentaires
ALTER TABLE products ADD CONSTRAINT uk_products_sku UNIQUE (sku);
ALTER TABLE products ADD CONSTRAINT uk_products_ean UNIQUE (ean);
