-- V4: Añadir columna bloqueado a la tabla auth.usuarios
ALTER TABLE auth.usuarios
    ADD COLUMN IF NOT EXISTS bloqueado BOOLEAN NOT NULL DEFAULT FALSE;
