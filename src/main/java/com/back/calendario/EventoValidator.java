package com.back.calendario;

import java.time.LocalDate;
import java.time.LocalTime;

public class EventoValidator {

    private EventoValidator() {
        // Utility class
    }

    private static final LocalTime HORA_MIN = LocalTime.of(7, 0);
    private static final LocalTime HORA_MAX = LocalTime.of(19, 0);

    public static void validate(LocalDate fecha, LocalTime hora, String lugar, String observaciones) {
        validate(fecha, hora, lugar, null, observaciones);
    }

    public static void validate(LocalDate fecha, LocalTime hora, String lugar, String entrevistador, String observaciones) {
        if (fecha == null || hora == null) {
            throw new IllegalArgumentException("La fecha y la hora son obligatorias");
        }
        if (hora.isBefore(HORA_MIN) || hora.isAfter(HORA_MAX)) {
            throw new IllegalArgumentException("La hora debe estar entre las 07:00 y las 19:00");
        }
        if (fecha.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha no puede ser anterior a hoy");
        }
        if (fecha.equals(LocalDate.now()) && hora.isBefore(LocalTime.now().withSecond(0).withNano(0))) {
            throw new IllegalArgumentException("La hora seleccionada ya pasó");
        }
        if (lugar != null && !lugar.isBlank()) {
            if (lugar.trim().length() < 3 || lugar.trim().length() > 200) {
                throw new IllegalArgumentException("La ubicación o enlace debe tener entre 3 y 200 caracteres");
            }
        }
        validateEntrevistador(entrevistador);
        validateObservaciones(observaciones);
    }

    public static void validateEntrevistador(String entrevistador) {
        if (entrevistador != null && !entrevistador.isBlank()) {
            if (entrevistador.length() > 200) {
                throw new IllegalArgumentException("El entrevistador no puede tener más de 200 caracteres");
            }
            if (!entrevistador.matches("^[A-Za-záéíóúÁÉÍÓÚñÑ\\s]+$")) {
                throw new IllegalArgumentException("El nombre del entrevistador solo debe contener letras");
            }
        }
    }

    public static void validateObservaciones(String observaciones) {
        if (observaciones != null && observaciones.length() > 500) {
            throw new IllegalArgumentException("Las observaciones no pueden tener más de 500 caracteres");
        }
    }
}
