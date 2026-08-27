package com.back.calendario;

public record EventoExtendedPropsDTO(
    Long candidatoId,
    String candidatoNombre,
    String tipo,
    String estado,
    String lugar,
    String vacante,
    String modalidad,
    String entrevistador,
    String observaciones
) {}
