package com.back.shared.event;

import java.time.LocalDate;
import java.time.LocalTime;

public record AccionCandidatoEntrevistaEvent(
    Long candidatoId,
    String candidatoNombre,
    String accion,
    LocalDate fecha,
    LocalTime hora,
    LocalDate nuevaFecha,
    LocalTime nuevaHora,
    String motivo,
    Long rrhhId,
    String rrhhEmail
) {
}
