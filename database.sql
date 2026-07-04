DROP TABLE IF EXISTS archivos;
DROP TABLE IF EXISTS usuarios;

CREATE TABLE usuarios (
    id SERIAL PRIMARY KEY,
    username TEXT NOT NULL,
    apellido TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    clave TEXT NOT NULL,
    telefono TEXT,
    rol TEXT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT FALSE,
    tokenactivacion TEXT UNIQUE
);

CREATE TABLE archivos (
    id SERIAL PRIMARY KEY,
    nombre TEXT,
    ubicacion TEXT,
    propietario TEXT,
    destinario TEXT,
    es_carpeta BOOLEAN
);

insert into usuarios(username, apellido, email, clave, telefono, rol, activo) values ('Esteban', 'Gomez', 'gomez@gmail.com', '$2a$10$JDYXxiV.Df.cj29mk19f3uUmiABNGiyHiidc8BMqSUd1hL49SvrwG', '123123123', 'ROLE_ADMIN', true);