CREATE DATABASE IF NOT EXISTS Prueba;
USE Prueba;

CREATE TABLE IF NOT EXISTS individuo(
    Id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    Nombre VARCHAR(15),
    Apellido VARCHAR(15),
    Edad INT,
    Correo VARCHAR(50),
    Telefono VARCHAR(29)
);

INSERT INTO individuo (Nombre, Apellido, Edad, Correo, Telefono) VALUES
("Esteban", "Florez",20,"esteban@example.com","3208722328"),
("Giovanni", "Castro",40,"giovanni@example.com","3569874128"),
("Pilar", "Del Rocio",40,"pilar@example.com","321654987"),
("Isabela", "Gomez",12,"isabela@example.com","123456789");

CREATE TABLE IF NOT EXISTS perfil (
    id_perfil INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50)
);

INSERT INTO perfil (nombre) VALUES
("Gerente"),
("Administrador"),
("Usuario");

CREATE TABLE IF NOT EXISTS usuario (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50),
    password VARCHAR(100),
    activo BOOLEAN,
    id_perfil INT,
    FOREIGN KEY (id_perfil) REFERENCES perfil(id_perfil)
);

INSERT INTO usuario (username, password, activo, id_perfil) VALUES
("admin", "$2a$10$icw.JZU7Vf2gmEKQg9WRJeklKjLBkcOe4bWpmbh4EhMw8Tp.Cogra", TRUE, 2),
("user1", "$2a$10$awwvhOqqmYdygIWPbhGgp.iNrU0kCqeeBduCdBwc9A5BLVFzpAJWi", TRUE, 3);

SELECT * FROM individuo;
SELECT * FROM perfil;
SELECT * FROM usuario;
SELECT u.username, u.activo, p.nombre AS perfil
FROM usuario u
INNER JOIN perfil p ON u.id_perfil = p.id_perfil;