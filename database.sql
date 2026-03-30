create table if not exists usuarios (
	id serial primary key,
	username text,
	correo text,
	clave text,
	cedula text,
	role text
);

create table if not exists archivos (
        id serial primary key,
        nombre text,
        ubicacion text,
        propietario text,
        destinario text,
        es_carpeta boolean
);
