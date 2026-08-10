package com.back.shared.event;

import com.back.shared.dto.EntrevistaEmailDTO;

public record EntrevistaAgendadaEvent(
    Long eventoId,
    Long candidatoId,
    String candidatoNombre,
    String candidatoEmail,
    Long rrhhId,
    String rrhhEmail,
    EntrevistaEmailDTO eventoDto,
    String tipo,
    String fechaStr
) {}
