CREATE TABLE billing_records (
    id BIGSERIAL PRIMARY KEY,
    customer_id VARCHAR(64) NOT NULL,
    invoice_number VARCHAR(64) NOT NULL,
    amount NUMERIC(19, 4) NOT NULL,
    currency VARCHAR(8) NOT NULL,
    transaction_date DATE NOT NULL,
    description VARCHAR(512)
);
