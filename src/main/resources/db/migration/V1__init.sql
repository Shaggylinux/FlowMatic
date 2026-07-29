CREATE SCHEMA IF NOT EXISTS auth;
CREATE SCHEMA IF NOT EXISTS drive;
CREATE SCHEMA IF NOT EXISTS shared;
CREATE SCHEMA IF NOT EXISTS admin;
CREATE SCHEMA IF NOT EXISTS candidatos;
CREATE SCHEMA IF NOT EXISTS calendario;
CREATE SCHEMA IF NOT EXISTS notificaciones;
CREATE SCHEMA IF NOT EXISTS seguridad;

CREATE TABLE auth.usuarios (
    id serial PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    clave VARCHAR(255) NOT NULL,
    rol VARCHAR(50) NOT NULL,
    activo BOOLEAN DEFAULT FALSE
);

CREATE TABLE drive.archivos (
    id serial PRIMARY KEY,
    nombre VARCHAR(500) NOT NULL,
    ubicacion TEXT,
    propietario TEXT,
    destinario TEXT,
    es_carpeta BOOLEAN,
    etapa VARCHAR(255),
    tipo_documento VARCHAR(255)
);

CREATE TABLE shared.historial (
    id serial PRIMARY KEY,
    fecha VARCHAR(100),
    estado_anterior TEXT,
    estado_nuevo TEXT,
    responsable TEXT
);

CREATE TABLE admin.actividades (
    id serial PRIMARY KEY,
    accion VARCHAR(255),
    detalle TEXT,
    usuario_email VARCHAR(255),
    ip VARCHAR(50),
    tipo VARCHAR(50),
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE admin.configuraciones (
    id serial PRIMARY KEY,
    clave VARCHAR(255) NOT NULL UNIQUE,
    valor TEXT NOT NULL
);

CREATE TABLE admin.rrhh (
    id bigint PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    apellido VARCHAR(255),
    telefono VARCHAR(20),
    foto_url TEXT
);

CREATE TABLE admin.administradores (
    id bigint PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    apellido VARCHAR(255)
);

CREATE TABLE candidatos.candidatos (
    id bigint PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    apellido VARCHAR(255),
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

CREATE TABLE calendario.eventos (
    id serial PRIMARY KEY,
    titulo VARCHAR(255),
    descripcion TEXT,
    fecha DATE,
    hora TIME,
    tipo VARCHAR(50),
    candidato_id bigint,
    rrhh_id bigint,
    estado VARCHAR(50)
);

CREATE TABLE notificaciones.notificaciones (
    id serial PRIMARY KEY,
    tipo VARCHAR(50),
    mensaje TEXT,
    usuario_id bigint,
    leido BOOLEAN DEFAULT FALSE,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    enlace TEXT,
    nombre_relacionado VARCHAR(255)
);

CREATE TABLE seguridad.login_attempts (
    id serial PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    attempts INTEGER NOT NULL DEFAULT 0,
    blocked_until TIMESTAMP
);

insert into auth.usuarios(email, clave, rol, activo)
values ('admin@flowmatic.com', '$2a$10$JDYXxiV.Df.cj29mk19f3uUmiABNGiyHiidc8BMqSUd1hL49SvrwG', 'ROLE_ADMINISTRADOR', true);

insert into admin.administradores(id, username, apellido)
values (1, 'Admin', 'FlowMatic');
