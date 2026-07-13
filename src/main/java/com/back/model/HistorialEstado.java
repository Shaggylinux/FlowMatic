package com.back.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "historial")
public class HistorialEstado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "estado_anterior")
    private String estadoAnterior;

    @Column(name = "estado_nuevo")
    private String estadoNuevo;
    private String fecha;
    private String responsable;
}