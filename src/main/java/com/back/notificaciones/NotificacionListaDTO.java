package com.back.notificaciones;

import java.util.List;

public record NotificacionListaDTO(
    List<NotificacionDTO> notificaciones,
    long total
) {}
