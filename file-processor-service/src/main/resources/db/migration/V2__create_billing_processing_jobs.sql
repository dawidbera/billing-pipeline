-- Purpose: Create billing_processing_jobs table for tracking asynchronous processing jobs.
CREATE TABLE billing_processing_jobs (
    id BIGSERIAL PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    processed_records INTEGER,
    total_amount NUMERIC(19, 4),
    error_message VARCHAR(1024)
);
