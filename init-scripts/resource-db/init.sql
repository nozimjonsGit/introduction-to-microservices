CREATE TABLE IF NOT EXISTS resources (
    id         BIGSERIAL PRIMARY KEY,
    file_key   TEXT        NOT NULL UNIQUE,
    bucket     TEXT        NOT NULL,
    state      VARCHAR(50) NOT NULL,
    path       TEXT        NOT NULL,
    created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS outbox_event
(
    id            BIGSERIAL PRIMARY KEY,
    event_type    VARCHAR(50) NOT NULL,
    resource_id   BIGINT      NOT NULL,
    bucket        TEXT        NOT NULL,
    path          TEXT        NOT NULL,
    file_key      TEXT        NOT NULL,
    resource_data BYTEA,
    CONSTRAINT chk_event_type CHECK (event_type IN ('CREATE', 'DELETE'))
);

ALTER TABLE outbox_event
    ADD COLUMN trace_id VARCHAR(255),
    ADD COLUMN span_id  VARCHAR(255);
