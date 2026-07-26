package com.back.candidatos;

public record DetalleCandidatoDTO(
    Long id,
    String nombre,
    String apellido,
    String email,
    String telefono,
    String cargo,
    String ciudad,
    String tecnologias,
    String idiomas,
    Integer experiencia,
    String disponibilidad,
    String estado,
    String procesoActual,
    String fotoUrl,
    int matchScore,
    String matchLabel,
    String ultimaActualizacion
) {}
