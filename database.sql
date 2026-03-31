create table if not exists usuarios (
    id serial primary key,
    username text not null,
    email text not null unique,
    clave text not null,
    telefono text,             
    rol text not null,          
    activo boolean not null default false,
    tokenActivacion text unique
);

create table if not exists archivos (
    id serial primary key,
    nombre text,
    ubicacion text,
    propietario text,
    destinario text,
    es_carpeta boolean
);