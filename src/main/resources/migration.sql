-- Migration: Separar Usuario (auth) en modelos por rol
-- Ejecutar solo cuando la aplicación esté detenida

-- 1. Crear tabla candidatos (hereda PK de usuarios)
CREATE TABLE candidatos (
    id BIGINT PRIMARY KEY REFERENCES usuarios(id),
    username VARCHAR(255) NOT NULL,
    apellido VARCHAR(255) NOT NULL,
    telefono VARCHAR(255),
    estado VARCHAR(255),
    cargo VARCHAR(255),
    ciudad VARCHAR(255),
    tecnologias TEXT,
    idiomas VARCHAR(255),
    experiencia INTEGER,
    disponibilidad VARCHAR(255),
    proceso_actual VARCHAR(255),
    foto_url VARCHAR(255),
    ultima_actualizacion TIMESTAMP
);

-- 2. Crear tabla rrhh
CREATE TABLE rrhh (
    id BIGINT PRIMARY KEY REFERENCES usuarios(id),
    username VARCHAR(255) NOT NULL,
    apellido VARCHAR(255) NOT NULL,
    telefono VARCHAR(255),
    foto_url VARCHAR(255)
);

-- 3. Crear tabla administradores
CREATE TABLE administradores (
    id BIGINT PRIMARY KEY REFERENCES usuarios(id),
    username VARCHAR(255) NOT NULL,
    apellido VARCHAR(255) NOT NULL
);

-- 4. Migrar datos de candidatos
INSERT INTO candidatos (id, username, apellido, telefono, estado, cargo, ciudad, tecnologias, idiomas, experiencia, disponibilidad, proceso_actual, foto_url, ultima_actualizacion)
SELECT id, username, apellido, telefono, estado, cargo, ciudad, tecnologias, idiomas, experiencia, disponibilidad, proceso_actual, foto_url, ultima_actualizacion
FROM usuarios WHERE rol = 'ROLE_CANDIDATO';

-- 5. Migrar datos de RRHH
INSERT INTO rrhh (id, username, apellido, telefono, foto_url)
SELECT id, username, apellido, telefono, foto_url
FROM usuarios WHERE rol = 'ROLE_RRHH';

-- 6. Migrar datos de Administradores
INSERT INTO administradores (id, username, apellido)
SELECT id, username, apellido
FROM usuarios WHERE rol = 'ROLE_ADMINISTRADOR';

-- 7. Eliminar columnas de perfil de usuarios (dejar solo auth)
ALTER TABLE usuarios
  DROP COLUMN IF EXISTS username,
  DROP COLUMN IF EXISTS apellido,
  DROP COLUMN IF EXISTS telefono,
  DROP COLUMN IF EXISTS estado,
  DROP COLUMN IF EXISTS cargo,
  DROP COLUMN IF EXISTS ciudad,
  DROP COLUMN IF EXISTS linkedin,
  DROP COLUMN IF EXISTS tecnologias,
  DROP COLUMN IF EXISTS idiomas,
  DROP COLUMN IF EXISTS experiencia,
  DROP COLUMN IF EXISTS disponibilidad,
  DROP COLUMN IF EXISTS proceso_actual,
  DROP COLUMN IF EXISTS foto_url,
  DROP COLUMN IF EXISTS ultima_actualizacion;

-- 8. Verificar migración
SELECT 'CANDIDATOS: ' || COUNT(*) FROM candidatos;
SELECT 'RRHH: ' || COUNT(*) FROM rrhh;
SELECT 'ADMINISTRADORES: ' || COUNT(*) FROM administradores;
SELECT 'USUARIOS (auth only): ' || COUNT(*) FROM usuarios;
