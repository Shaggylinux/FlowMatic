create table if not exists usuarios (
	id serial primary key,
	username text,
	correo text,
	clave text,
	cedula text,
	role text
);
