create database gestion;

create table if not exists usuarios (
	id serial primary key,
	nombre text,
	correo text,
	clave text,
	cedula text
);
