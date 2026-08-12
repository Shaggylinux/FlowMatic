package com.back.notificaciones;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

    @Mock
    private NotificacionRepository notificacionRepository;

    private NotificacionService service;

    @BeforeEach
    void setUp() {
        service = new NotificacionService(notificacionRepository);
    }

    private Notificacion notificacion(Long id, Long candidatoId, boolean leida) {
        Notificacion n = new Notificacion();
        n.setId(id);
        n.setCandidatoId(candidatoId);
        n.setLeida(leida);
        return n;
    }

    @Test
    void marcarLeida_candidatoIntentaLeerAjeno_noMarca() {
        Notificacion ajena = notificacion(1L, 999L, false);
        when(notificacionRepository.findById(1L)).thenReturn(Optional.of(ajena));

        service.marcarLeida(1L, true, 2L);

        assertThat(ajena.isLeida()).isFalse();
        verify(notificacionRepository, never()).save(any());
    }

    @Test
    void marcarLeida_candidatoDuenoMarca() {
        Notificacion propia = notificacion(2L, 5L, false);
        when(notificacionRepository.findById(2L)).thenReturn(Optional.of(propia));

        service.marcarLeida(2L, true, 5L);

        assertThat(propia.isLeida()).isTrue();
        verify(notificacionRepository).save(propia);
    }

    @Test
    void marcarLeida_globalPuedeMarcarCualquiera() {
        Notificacion deCandidato = notificacion(3L, 5L, false);
        when(notificacionRepository.findById(3L)).thenReturn(Optional.of(deCandidato));

        service.marcarLeida(3L, false, null);

        assertThat(deCandidato.isLeida()).isTrue();
        verify(notificacionRepository).save(deCandidato);
    }

    @Test
    void marcarTodasLeidas_candidatoUsaSoloSusNotificaciones() {
        when(notificacionRepository.findByLeidaFalseAndCandidatoIdOrderByFechaDesc(5L))
                .thenReturn(List.of(notificacion(4L, 5L, false)));

        service.marcarTodasLeidas(true, 5L);

        verify(notificacionRepository).findByLeidaFalseAndCandidatoIdOrderByFechaDesc(5L);
        verify(notificacionRepository).save(any());
    }

    @Test
    void marcarTodasLeidas_globalMarcaTodasLasNoLeidas() {
        when(notificacionRepository.findByLeidaFalseOrderByFechaDesc())
                .thenReturn(List.of(notificacion(6L, null, false), notificacion(7L, 5L, false)));

        service.marcarTodasLeidas(false, null);

        verify(notificacionRepository).findByLeidaFalseOrderByFechaDesc();
        verify(notificacionRepository, org.mockito.Mockito.times(2)).save(any());
    }

    @Test
    void eliminarPorCandidato_borraTodasSusNotificaciones() {
        service.eliminarPorCandidato(5L);
        verify(notificacionRepository).deleteByCandidatoId(5L);
    }

    @Test
    void contarNoLeidasGlobales_delegaEnRepositorio() {
        when(notificacionRepository.countByLeidaFalseAndCandidatoIdIsNull()).thenReturn(3L);

        assertThat(service.contarNoLeidasGlobales()).isEqualTo(3L);
    }
}
