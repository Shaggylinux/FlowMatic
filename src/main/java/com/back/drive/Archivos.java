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

    @Column(name = "categoria_documento")
    private String categoriaDocumento = "Requerido";

    @Column(name = "fecha_subida")
    private java.time.LocalDateTime fechaSubida;

    @Transient
    private String tamanoFormateado = "--";

    @Transient
    private String fechaModificacion = "--";

    @Transient
    private String nombreCandidatoStr = "General";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidato_id")
    private com.back.auth.Usuario candidato;

    public String getCategoriaCalculada() {
        if (categoriaDocumento != null && !categoriaDocumento.isBlank()) {
            return categoriaDocumento;
        }
        if (nombre != null) {
            String n = nombre.toLowerCase();
            if (n.contains("portafolio") || n.contains("certificados_estudios") || n.contains("referencias") || n.contains("opcional")) {
                return "Opcional";
            }
        }
        return "Requerido";
    }
}