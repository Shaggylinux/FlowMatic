package com.back.calendario;

public record EventoCandidatoDTO(
    Long id,
    String titulo,
    String fecha,
    String hora,
    String estado,
    String tipo,
    String lugar,
    String observaciones
) {}
