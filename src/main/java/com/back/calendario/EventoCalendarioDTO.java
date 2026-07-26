package com.back.calendario;

import java.util.Map;

public record EventoCalendarioDTO(
    Long id,
    String title,
    String start,
    String backgroundColor,
    String borderColor,
    String textColor,
    Map<String, Object> extendedProps
) {}
