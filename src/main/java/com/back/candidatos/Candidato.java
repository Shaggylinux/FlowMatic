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
    private java.time.LocalDateTime ultimaActualizacion;

    private String nombres;

    @Column(name = "tipo_documento")
    private String tipoDocumento;

    @Column(name = "numero_documento")
    private String numeroDocumento;

    private String genero;

    @Column(name = "estado_civil")
    private String estadoCivil;

    @Column(name = "fecha_nacimiento")
    private java.time.LocalDate fechaNacimiento;

    private String nacionalidad;

    @Column(name = "telefono_fijo")
    private String telefonoFijo;

    private String direccion;

    @Column(name = "sobre_mi", columnDefinition = "TEXT")
    private String sobreMi;

    @Column(name = "area_profesional")
    private String areaProfesional;

    @Column(name = "pretension_salarial")
    private String pretensionSalarial;

    @Column(name = "modalidad_trabajo")
    private String modalidadTrabajo;

    @Column(name = "formacion_json", columnDefinition = "TEXT")
    private String formacionJson;

    @Column(name = "experiencia_json", columnDefinition = "TEXT")
    private String experienciaJson;

    @Column(name = "idiomas_json", columnDefinition = "TEXT")
    private String idiomasJson;
}
