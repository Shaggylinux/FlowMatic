package com.back.shared.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record EntrevistaEmailDTO(
    LocalDate fecha,
    LocalTime hora,
    String tipo,
    String lugar,
    String observaciones
) {}
