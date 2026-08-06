package com.back.auth;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "usuarios", schema = "auth")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Ingresa un correo v\u00e1lido")
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @NotBlank(message = "La contrase\u00f1a es obligatoria")
    @Column(nullable = false)
    private String clave;

    @Column(nullable = false)
    private String rol;

    @Column(nullable = false)
    private boolean activo = false;

    @Column(nullable = false)
    private boolean bloqueado = false;

}
