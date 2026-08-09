package com.back.drive;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "archivos", schema = "drive")
public class Archivos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String ubicacion;
    private String propietario;
    private String destinario;

    private boolean esCarpeta;

    private String etapa;
    private String tipoDocumento;
    
    @Column(name = "estado_documento")
    private String estadoDocumento = "Pendiente";
    
    private String observacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidato_id")
    private com.back.auth.Usuario candidato;
}