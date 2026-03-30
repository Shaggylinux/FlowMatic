package com.back.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

//Anotación @Data para Lombok genera automaticamente todo los getter and setter, squals(), hashCode() y toString().//
@Data
//Le dice a JPA que esta clase representa una tabla en la base de datos//
@Entity
//Nombre de la tabla en la base de datos//
@Table(name = "usuarios")
public class Usuario {
    //Llave primaria de la base//
    @Id
    //PosgreSQL asigna el numero automaticamente.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //Long numero entero grande para suficientes usuarios//
    private Long id;

    //Usamos las validaciones, la anotamos con @NotBlanck para rechazar espacios vacios, null. si falla se aplica la restriccion con el mensaje "El campo es obligatorio"//
    @NotBlank(message = "El nombre es obligatorio")
    //La columna no puede ser nula a nivel de bases de datos//
    @Column(nullable = false)
    private String username;

    @NotBlank(message = "El correo es obligatorio")
    //Usamos anotaciñon @Email para validar que sea en el formato correcto//
    @Email(message = "Ingresa un correo válido")
    //En la base de datos no puede existir filas con el mismo correo//
    @Column(nullable = false, unique = true)
    private String correo;

    @NotBlank(message = "La contraseña es obligatoria")
    //Usamos la onotación @Size para validar que la contraseña deber ser minimo de 8 caracteres//
    @Size(min = 8, message = "La contraseña debe tener mínimo 8 caracteres")
    @Column(nullable = false)
    private String clave;

    @Column(nullable = false)
    private String rol;

    @Column(nullable = false)
    private String telefono;
    @Column(nullable = false)
    private boolean activo = false;

    @Column(unique = true)
    private String tokenActivacion;
}
