CREATE TABLE IF NOT EXISTS seguridad.login_attempts (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    attempts INT NOT NULL DEFAULT 0,
    blocked_until TIMESTAMP
);
