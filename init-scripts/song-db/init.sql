CREATE TABLE IF NOT EXISTS songs (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    artist VARCHAR(255),
    album VARCHAR(255),
    length VARCHAR(255),
    resource_id VARCHAR(255),
    year VARCHAR(4)
);