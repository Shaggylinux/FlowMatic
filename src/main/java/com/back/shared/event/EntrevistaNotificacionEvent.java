package com.back.shared.event;

public record EntrevistaNotificacionEvent(
    Long candidatoId,
    String candidatoNombre,
    String accion,
    String mensajeDetalle
) {}
