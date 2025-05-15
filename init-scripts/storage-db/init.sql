CREATE TABLE IF NOT EXISTS storages
(
    id     BIGSERIAL PRIMARY KEY,
    type   VARCHAR(50)  NOT NULL CHECK (type IN ('STAGING', 'PERMANENT')),
    bucket VARCHAR(255) NOT NULL,
    path   VARCHAR(255) NOT NULL
);

INSERT INTO storages (type, bucket, path)
VALUES ('STAGING', 'staging-bucket', 'staging/path'),
       ('PERMANENT', 'permanent-bucket', 'permanent/path')