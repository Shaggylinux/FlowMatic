package com.back.admin;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "rrhh", schema = "admin")
public class RRHH {

    @Id
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String apellido;

    private String telefono;

    @Column(name = "foto_url")
    private String fotoUrl;

    @Column(name = "documento", length = 20)
    private String documento;

    @Column(name = "cargo", length = 100)
    private String cargo;

    @Column(name = "ultimo_acceso")
    private java.time.LocalDateTime ultimoAcceso;
}
