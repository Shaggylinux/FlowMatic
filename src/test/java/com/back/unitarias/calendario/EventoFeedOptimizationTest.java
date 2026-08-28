package com.back.unitarias.calendario;

import com.back.calendario.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventoFeedOptimizationTest {

    @Mock
    private EventoRepository eventoRepository;

    @InjectMocks
    private EventoService eventoService;

    @Test
    void testEventoCalendarioDTOFactoryMapping() {
        Evento evento = new Evento();
        evento.setId(101L);
        evento.setCandidatoId(55L);
        evento.setCandidatoNombre("Laura Restrepo");
        evento.setFecha(LocalDate.of(2026, 9, 15));
        evento.setHora(LocalTime.of(14, 30));
        evento.setTipo("Entrevista Técnica");
        evento.setEstado("CONFIRMADO");
        evento.setLugar("https://meet.google.com/xyz");
        evento.setVacante("Desarrollador Java Senior");
        evento.setModalidad("VIRTUAL");
        evento.setEntrevistador("Carlos RRHH");
        evento.setObservaciones("Revisar arquitectura");
        evento.setRrhhId(1L);

        EventoCalendarioDTO dto = EventoCalendarioDTO.from(evento);

        assertThat(dto.id()).isEqualTo(101L);
        assertThat(dto.title()).isEqualTo("Laura Restrepo — 14:30");
        assertThat(dto.start()).isEqualTo("2026-09-15T14:30");
        assertThat(dto.backgroundColor()).isEqualTo("#DCFCE7");
        assertThat(dto.borderColor()).isEqualTo("#22C55E");
        assertThat(dto.textColor()).isEqualTo("#166534");

        EventoExtendedPropsDTO props = dto.extendedProps();
        assertThat(props).isNotNull();
        assertThat(props.candidatoId()).isEqualTo(55L);
        assertThat(props.candidatoNombre()).isEqualTo("Laura Restrepo");
        assertThat(props.tipo()).isEqualTo("Entrevista Técnica");
        assertThat(props.estado()).isEqualTo("CONFIRMADO");
        assertThat(props.lugar()).isEqualTo("https://meet.google.com/xyz");
        assertThat(props.vacante()).isEqualTo("Desarrollador Java Senior");
        assertThat(props.modalidad()).isEqualTo("VIRTUAL");
        assertThat(props.entrevistador()).isEqualTo("Carlos RRHH");
        assertThat(props.observaciones()).isEqualTo("Revisar arquitectura");
    }

    @Test
    void testEventoColorThemeMapping() {
        assertThat(EventoColorTheme.of("CONFIRMADO")).isEqualTo(EventoColorTheme.CONFIRMADO);
        assertThat(EventoColorTheme.of("REPROGRAMADO")).isEqualTo(EventoColorTheme.REPROGRAMADO);
        assertThat(EventoColorTheme.of("CANCELADO")).isEqualTo(EventoColorTheme.CANCELADO);
        assertThat(EventoColorTheme.of("REALIZADA")).isEqualTo(EventoColorTheme.REALIZADA);
        assertThat(EventoColorTheme.of("PENDIENTE")).isEqualTo(EventoColorTheme.DEFAULT);
        assertThat(EventoColorTheme.of(null)).isEqualTo(EventoColorTheme.DEFAULT);
        assertThat(EventoColorTheme.of("DESCONOCIDO")).isEqualTo(EventoColorTheme.DEFAULT);
    }

    @Test
    void testObtenerFeedCalendarioTransformsEfficiently() {
        LocalDate start = LocalDate.of(2026, 9, 1);
        LocalDate end = LocalDate.of(2026, 9, 30);

        Evento e1 = new Evento();
        e1.setId(1L);
        e1.setCandidatoId(10L);
        e1.setCandidatoNombre("Candidato 1");
        e1.setFecha(LocalDate.of(2026, 9, 5));
        e1.setHora(LocalTime.of(9, 0));
        e1.setEstado("PENDIENTE");

        Evento e2 = new Evento();
        e2.setId(2L);
        e2.setCandidatoId(20L);
        e2.setCandidatoNombre("Candidato 2");
        e2.setFecha(LocalDate.of(2026, 9, 10));
        e2.setHora(LocalTime.of(11, 0));
        e2.setEstado("CANCELADO");

        when(eventoRepository.findFiltered(eq(start), eq(end), isNull(), isNull(), isNull()))
                .thenReturn(List.of(e1, e2));

        List<EventoCalendarioDTO> feed = eventoService.obtenerFeedCalendario(start, end, null, "   ", null);

        assertThat(feed).hasSize(2);
        assertThat(feed.get(0).title()).isEqualTo("Candidato 1 — 09:00");
        assertThat(feed.get(0).backgroundColor()).isEqualTo("#DBEAFE");
        assertThat(feed.get(1).title()).isEqualTo("Candidato 2 — 11:00");
        assertThat(feed.get(1).backgroundColor()).isEqualTo("#FEE2E2");
    }
}
