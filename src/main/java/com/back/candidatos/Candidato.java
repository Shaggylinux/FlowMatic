package com.back.candidatos;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "candidatos", schema = "candidatos")
public class Candidato {

    @Id
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String apellido;

    private String telefono;

    private String estado;

    private String cargo;

    private String ciudad;

    @Column(columnDefinition = "TEXT")
    private String tecnologias;

    private String idiomas;

    private Integer experiencia;

    private String disponibilidad;

    @Column(name = "proceso_actual")
    private String procesoActual;

    @Column(name = "foto_url")
    private String fotoUrl;

    @Column(name = "rrhh_email")
    private String rrhhEmail;

    @Column(name = "ultima_actualizacion")
    private LocalDateTime ultimaActualizacion;
}
