-- V2 Migration: Split tokens and add creation dates

ALTER TABLE usuarios RENAME COLUMN tokenactivacion TO token_activacion;

ALTER TABLE usuarios ADD COLUMN fecha_creacion_token TIMESTAMP;
ALTER TABLE usuarios ADD COLUMN token_reset_password VARCHAR(255) UNIQUE;
ALTER TABLE usuarios ADD COLUMN fecha_creacion_token_reset TIMESTAMP;
