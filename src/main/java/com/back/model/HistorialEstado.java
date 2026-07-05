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
    private String estado_anterior;
    private String estado_nuevo;
    private String fecha;
    private String responsable;
}