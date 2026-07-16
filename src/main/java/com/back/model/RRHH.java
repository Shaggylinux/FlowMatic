package com.back.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "rrhh")
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
}
