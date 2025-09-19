/*
 * @path src/main/resources/db/migration/V5__ventes_init.sql
 * @description Création des tables ventes : quotes, orders, order_lines, payments
 */
CREATE TABLE quotes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    total DECIMAL(12,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_quote_customer FOREIGN KEY(customer_id) REFERENCES customers(id)
);

CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    quote_id BIGINT,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_quote FOREIGN KEY(quote_id) REFERENCES quotes(id)
);

CREATE TABLE order_lines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_line_order FOREIGN KEY(order_id) REFERENCES orders(id),
    CONSTRAINT fk_line_product FOREIGN KEY(product_id) REFERENCES products(id)
);

CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    paid_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_order FOREIGN KEY(order_id) REFERENCES orders(id)
);
