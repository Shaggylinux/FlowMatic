package com.back.shared.dto;

public record CvDataDTO(
    String nombre,
    String apellido,
    String email,
    String telefono,
    String ciudad,
    String cargo,
    int experiencia,
    String tecnologias,
    String idiomas,
    String disponibilidad
) {}
