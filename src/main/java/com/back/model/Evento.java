package com.back.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "eventos")
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long candidatoId;

    @Column(nullable = false)
    private String candidatoNombre;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime hora;

    private String tipo;

    private String estado;

    private String lugar;

    private String vacante;

    private String modalidad;

    private String entrevistador;

    private String observaciones;

    @Column(nullable = false)
    private Long rrhhId;
}
