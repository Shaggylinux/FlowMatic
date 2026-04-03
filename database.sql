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

-- 📁 Tabla de archivos
CREATE TABLE archivos (
    id SERIAL PRIMARY KEY,
    nombre TEXT,
    ubicacion TEXT,
    propietario TEXT,
    destinario TEXT,
    es_carpeta BOOLEAN
);

