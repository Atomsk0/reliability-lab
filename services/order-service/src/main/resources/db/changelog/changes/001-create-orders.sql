-- liquibase formatted sql

-- changeset lab:001-create-orders
CREATE TABLE orders (
    id            UUID PRIMARY KEY,
    customer_id   VARCHAR(64)    NOT NULL,
    status        VARCHAR(16)    NOT NULL,
    total_amount  NUMERIC(12, 2) NOT NULL,
    created_at    TIMESTAMPTZ    NOT NULL
);
-- rollback DROP TABLE orders;
