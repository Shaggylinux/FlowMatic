CREATE SCHEMA IF NOT EXISTS auth;
CREATE SCHEMA IF NOT EXISTS drive;
CREATE SCHEMA IF NOT EXISTS shared;
CREATE SCHEMA IF NOT EXISTS admin;
CREATE SCHEMA IF NOT EXISTS candidatos;
CREATE SCHEMA IF NOT EXISTS calendario;
CREATE SCHEMA IF NOT EXISTS notificaciones;
CREATE SCHEMA IF NOT EXISTS seguridad;

CREATE TABLE IF NOT EXISTS auth.usuarios (
    id bigserial PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    clave VARCHAR(255) NOT NULL,
    rol VARCHAR(50) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS drive.archivos (
    id bigserial PRIMARY KEY,
    nombre VARCHAR(500) NOT NULL,
    ubicacion TEXT,
    propietario TEXT,
    destinario TEXT,
    es_carpeta BOOLEAN NOT NULL DEFAULT FALSE,
    etapa VARCHAR(255),
    tipo_documento VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS shared.historial (
    id bigserial PRIMARY KEY,
    fecha VARCHAR(100),
    estado_anterior TEXT,
    estado_nuevo TEXT,
    responsable TEXT
);

CREATE TABLE IF NOT EXISTS admin.actividades (
    id bigserial PRIMARY KEY,
    accion VARCHAR(50) NOT NULL,
    descripcion VARCHAR(500) NOT NULL,
    realizado_por VARCHAR(100),
    tipo VARCHAR(50) NOT NULL,
    fecha TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS admin.configuraciones (
    clave VARCHAR(100) PRIMARY KEY,
    valor VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS admin.rrhh (
    id bigint PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    apellido VARCHAR(255) NOT NULL,
    telefono VARCHAR(20),
    foto_url TEXT
);

CREATE TABLE IF NOT EXISTS admin.administradores (
    id bigint PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    apellido VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS candidatos.candidatos (
    id bigint PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    apellido VARCHAR(255) NOT NULL,
    telefono VARCHAR(20),
    estado VARCHAR(50),
    cargo VARCHAR(255),
    ciudad VARCHAR(255),
    tecnologias TEXT,
    idiomas TEXT,
    experiencia INTEGER,
    disponibilidad VARCHAR(50),
    proceso_actual VARCHAR(100),
    foto_url TEXT,
    ultima_actualizacion TIMESTAMP
);

CREATE TABLE IF NOT EXISTS calendario.eventos (
    id bigserial PRIMARY KEY,
    candidato_id bigint NOT NULL,
    candidato_nombre VARCHAR(255) NOT NULL,
    fecha DATE NOT NULL,
    hora TIME NOT NULL,
    tipo VARCHAR(50),
    estado VARCHAR(50),
    lugar VARCHAR(255),
    vacante VARCHAR(255),
    modalidad VARCHAR(50),
    entrevistador VARCHAR(255),
    observaciones TEXT,
    rrhh_id bigint NOT NULL
);

CREATE TABLE IF NOT EXISTS notificaciones.notificaciones (
    id bigserial PRIMARY KEY,
    tipo VARCHAR(50) NOT NULL,
    mensaje VARCHAR(500) NOT NULL,
    candidato_id bigint,
    candidato_nombre VARCHAR(255),
    fecha TIMESTAMP NOT NULL,
    leida BOOLEAN NOT NULL DEFAULT FALSE,
    enlace TEXT
);

CREATE TABLE IF NOT EXISTS seguridad.login_attempts (
    id bigserial PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    attempts INTEGER NOT NULL DEFAULT 0,
    blocked_until TIMESTAMP
);

INSERT INTO auth.usuarios(email, clave, rol, activo)
SELECT 'admin@flowmatic.com', '$2a$10$JDYXxiV.Df.cj29mk19f3uUmiABNGiyHiidc8BMqSUd1hL49SvrwG', 'ROLE_ADMINISTRADOR', true
WHERE NOT EXISTS (SELECT 1 FROM auth.usuarios WHERE email = 'admin@flowmatic.com');

INSERT INTO admin.administradores(id, username, apellido)
SELECT 1, 'Admin', 'FlowMatic'
WHERE NOT EXISTS (SELECT 1 FROM admin.administradores WHERE id = 1);
