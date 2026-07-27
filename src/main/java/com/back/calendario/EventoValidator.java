package com.back.calendario;

import java.time.LocalDate;
import java.time.LocalTime;

public class EventoValidator {

    private EventoValidator() {
        // Utility class
    }

    public static void validate(LocalDate fecha, LocalTime hora, String lugar, String observaciones) {
        if (fecha == null || hora == null) {
            throw new IllegalArgumentException("La fecha y la hora son obligatorias");
        }
        if (fecha.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha no puede ser anterior a hoy");
        }
        if (fecha.equals(LocalDate.now()) && hora.isBefore(LocalTime.now().withSecond(0).withNano(0))) {
            throw new IllegalArgumentException("La hora seleccionada ya pasó");
        }
        if (lugar != null && lugar.length() > 200) {
            throw new IllegalArgumentException("El lugar no puede tener más de 200 caracteres");
        }
        validateObservaciones(observaciones);
    }

    public static void validateObservaciones(String observaciones) {
        if (observaciones != null && observaciones.length() > 500) {
            throw new IllegalArgumentException("Las observaciones no pueden tener más de 500 caracteres");
        }
    }
}
