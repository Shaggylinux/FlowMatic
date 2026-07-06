CREATE TABLE usuarios (
    id serial PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    apellido VARCHAR(255),
    email VARCHAR(255) NOT NULL UNIQUE,
    clave VARCHAR(255) NOT NULL,
    telefono VARCHAR(20),
    tokenactivacion VARCHAR(255),
    rol VARCHAR(50) NOT NULL,
    activo BOOLEAN DEFAULT FALSE,
    estado VARCHAR(50),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
);

CREATE TABLE archivos (
    id serial PRIMARY KEY,
    nombre VARCHAR(500) NOT NULL,
    ubicacion TEXT,
    propietario TEXT,
    destinario TEXT,
    es_carpeta BOOLEAN
);

insert into usuarios(username, apellido, email, clave, telefono, rol, activo) values ('Esteban', 'Gomez', 'gomez@gmail.com', '$2a$10$JDYXxiV.Df.cj29mk19f3uUmiABNGiyHiidc8BMqSUd1hL49SvrwG', '123123123', 'ROLE_ADMINISTRADOR', true);

create table historial (
	id serial primary key,
	fecha date not null default NOW(),
	estado_anterior text,
	estado_nuevo text,
	responsable text
);
