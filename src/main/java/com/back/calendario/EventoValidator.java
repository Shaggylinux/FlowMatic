package com.back.calendario;

import java.time.LocalDate;
import java.time.LocalTime;

public class EventoValidator {

    public static void validate(LocalDate fecha, LocalTime hora, String lugar, String observaciones) {
        if (fecha.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha no puede ser anterior a hoy");
        }
        if (fecha.equals(LocalDate.now()) && hora.isBefore(LocalTime.now().withSecond(0).withNano(0))) {
            throw new IllegalArgumentException("La hora seleccionada ya pasó");
        }
        if (hora.isBefore(LocalTime.of(7, 0)) || hora.isAfter(LocalTime.of(19, 0))) {
            throw new IllegalArgumentException("La hora debe estar entre 07:00 y 19:00");
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
