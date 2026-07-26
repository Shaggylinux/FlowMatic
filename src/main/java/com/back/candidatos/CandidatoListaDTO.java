package com.back.candidatos;

public record CandidatoListaDTO(
    Long id,
    String nombre,
    String apellido,
    String ciudad,
    String cargo,
    String email,
    String telefono,
    String estado,
    String procesoActual,
    String ultimaActualizacion,
    int matchScore
) {}
