package com.back.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Ingresa un correo v\u00e1lido")
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @NotBlank(message = "La contrase\u00f1a es obligatoria")
    @Size(min = 8, message = "La contrase\u00f1a debe tener m\u00ednimo 8 caracteres")
    @Column(nullable = false)
    private String clave;

    @Column(nullable = false)
    private String rol;

    @Column(nullable = false)
    private boolean activo = false;

    @Column(unique = true)
    private String tokenactivacion;

    @Column(name = "fecha_creacion_token")
    private LocalDateTime fechaCreacionToken;
}
