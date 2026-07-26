package com.back.candidatos;

public record DocumentoDTO(
    Long id,
    String nombre,
    String tipoDocumento,
    String etapa,
    String estado,
    String ubicacion
) {}
