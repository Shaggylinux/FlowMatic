package com.back.notificaciones;

import java.time.LocalDateTime;

public record NotificacionDTO(
    Long id,
    String tipo,
    String mensaje,
    Long candidatoId,
    String candidatoNombre,
    LocalDateTime fecha,
    boolean leida,
    String enlace
) {}
