/*
 * @path src/main/resources/db/migration/V6__facturation_init.sql
 * @description Création des tables facturation : invoices, invoice_lines, invoice_status
 */
CREATE TABLE invoices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_invoice_order FOREIGN KEY(order_id) REFERENCES orders(id)
);

CREATE TABLE invoice_lines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_invline_invoice FOREIGN KEY(invoice_id) REFERENCES invoices(id),
    CONSTRAINT fk_invline_product FOREIGN KEY(product_id) REFERENCES products(id)
);

CREATE TABLE invoice_status (
    code VARCHAR(20) PRIMARY KEY,
    description VARCHAR(255) NOT NULL
);
