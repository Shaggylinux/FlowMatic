package com.back.shared;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "historial", schema = "shared")
public class Historial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "candidato_id")
    private Long candidatoId;

    private LocalDateTime fecha;

    @Column(name = "estado_anterior", columnDefinition = "TEXT")
    private String estadoAnterior;

    @Column(name = "estado_nuevo", columnDefinition = "TEXT")
    private String estadoNuevo;

    @Column(columnDefinition = "TEXT")
    private String responsable;
}
