package com.back.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "administradores")
public class Administrador {

    @Id
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String apellido;
}
