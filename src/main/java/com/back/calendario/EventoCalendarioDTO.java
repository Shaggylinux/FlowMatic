package com.back.calendario;

public record EventoCalendarioDTO(
    Long id,
    String title,
    String start,
    String backgroundColor,
    String borderColor,
    String textColor,
    EventoExtendedPropsDTO extendedProps
) {

    public static EventoCalendarioDTO from(Evento e) {
        String estado = e.getEstado() != null ? e.getEstado() : "PENDIENTE";
        EventoColorTheme theme = EventoColorTheme.of(estado);

        String nombre = e.getCandidatoNombre() != null ? e.getCandidatoNombre() : "";
        String horaStr = e.getHora() != null ? e.getHora().toString() : "";
        String title = horaStr.isEmpty() ? nombre : nombre + " — " + horaStr;

        String start = "";
        if (e.getFecha() != null) {
            start = e.getHora() != null ? e.getFecha() + "T" + e.getHora() : e.getFecha().toString();
        }

        EventoExtendedPropsDTO props = new EventoExtendedPropsDTO(
            e.getCandidatoId(),
            nombre,
            e.getTipo() != null ? e.getTipo() : "",
            estado,
            e.getLugar() != null ? e.getLugar() : "",
            e.getVacante() != null ? e.getVacante() : "",
            e.getModalidad() != null ? e.getModalidad() : "",
            e.getEntrevistador() != null ? e.getEntrevistador() : "",
            e.getObservaciones() != null ? e.getObservaciones() : ""
        );

        return new EventoCalendarioDTO(
            e.getId(),
            title,
            start,
            theme.bg(),
            theme.border(),
            theme.text(),
            props
        );
    }
}
