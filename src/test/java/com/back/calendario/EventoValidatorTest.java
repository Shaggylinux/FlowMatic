package com.back.calendario;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventoValidatorTest {

    @Test
    void validate_conDatosValidosNoLanzaExcepcion() {
        assertThatCode(() -> EventoValidator.validate(LocalDate.now().plusDays(2), LocalTime.of(10, 0), "https://meet.google.com/abc-defg-hij", "Juan Perez", null))
                .doesNotThrowAnyException();
    }

    @Test
    void validate_conDireccionFisicaNoLanzaExcepcion() {
        assertThatCode(() -> EventoValidator.validate(LocalDate.now().plusDays(2), LocalTime.of(10, 0), "Cra. 77 # 65j-66 SUR", "Juan Perez", null))
                .doesNotThrowAnyException();
    }

    @Test
    void validate_entrevistadorConNumerosLanzaExcepcion() {
        assertThatThrownBy(() -> EventoValidator.validate(LocalDate.now().plusDays(2), LocalTime.of(10, 0), "https://meet.google.com/abc-defg-hij", "Juan123", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("solo debe contener letras");
    }

    @Test
    void validate_sinFechaOHoraLanzaExcepcion() {
        assertThatThrownBy(() -> EventoValidator.validate(null, LocalTime.of(10, 0), null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La fecha y la hora son obligatorias");
        assertThatThrownBy(() -> EventoValidator.validate(LocalDate.now().plusDays(2), null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La fecha y la hora son obligatorias");
    }

    @Test
    void validate_horaFueraDeRangoLanzaExcepcion() {
        assertThatThrownBy(() -> EventoValidator.validate(LocalDate.now().plusDays(2), LocalTime.of(6, 0), null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("07:00");
        assertThatThrownBy(() -> EventoValidator.validate(LocalDate.now().plusDays(2), LocalTime.of(20, 0), null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("19:00");
    }

    @Test
    void validate_fechaPasadaLanzaExcepcion() {
        assertThatThrownBy(() -> EventoValidator.validate(LocalDate.now().minusDays(1), LocalTime.of(10, 0), null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no puede ser anterior a hoy");
    }

    @Test
    void validate_horaDeHoyYaPasadaLanzaExcepcion() {
        LocalTime pasada = LocalTime.now().minusMinutes(5);
        if (pasada.isBefore(LocalTime.of(7, 0)) || pasada.isAfter(LocalTime.of(19, 0))) {
            assertThatThrownBy(() -> EventoValidator.validate(LocalDate.now(), pasada, null, null))
                    .isInstanceOf(IllegalArgumentException.class);
            return;
        }
        assertThatThrownBy(() -> EventoValidator.validate(LocalDate.now(), pasada, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya pasó");
    }

    @Test
    void validate_lugarMayorA200LanzaExcepcion() {
        String lugarLargo = "x".repeat(201);
        assertThatThrownBy(() -> EventoValidator.validate(LocalDate.now().plusDays(2), LocalTime.of(10, 0), lugarLargo, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("200 caracteres");
    }

    @Test
    void validateObservaciones_masDe500LanzaExcepcion() {
        String largo = "x".repeat(501);
        assertThatThrownBy(() -> EventoValidator.validateObservaciones(largo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("500 caracteres");
        assertThatCode(() -> EventoValidator.validateObservaciones(null)).doesNotThrowAnyException();
        assertThatCode(() -> EventoValidator.validateObservaciones("motivo corto")).doesNotThrowAnyException();
    }
}
