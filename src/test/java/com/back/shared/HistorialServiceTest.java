package com.back.shared;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HistorialServiceTest {

    @Mock
    private HistorialRepository historialRepository;

    @InjectMocks
    private HistorialService historialService;

    @Test
    void registrarCambio_guardaRegistroCorrectamente() {
        when(historialRepository.save(any(Historial.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Historial result = historialService.registrarCambio(10L, "Registrado", "En pruebas", "rrhh@flowmatic.com");

        assertThat(result).isNotNull();
        assertThat(result.getCandidatoId()).isEqualTo(10L);
        assertThat(result.getEstadoAnterior()).isEqualTo("Registrado");
        assertThat(result.getEstadoNuevo()).isEqualTo("En pruebas");
        assertThat(result.getResponsable()).isEqualTo("rrhh@flowmatic.com");
        assertThat(result.getFecha()).isNotNull();

        verify(historialRepository).save(any(Historial.class));
    }

    @Test
    void registrarCambio_conValoresPorDefecto() {
        when(historialRepository.save(any(Historial.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Historial result = historialService.registrarCambio(5L, null, "Entrevista", null);

        assertThat(result).isNotNull();
        assertThat(result.getCandidatoId()).isEqualTo(5L);
        assertThat(result.getEstadoAnterior()).isEqualTo("Registrado");
        assertThat(result.getEstadoNuevo()).isEqualTo("Entrevista");
        assertThat(result.getResponsable()).isEqualTo("Sistema");
    }

    @Test
    void registrarCambio_conParametrosInvalidos_retornaNull() {
        Historial r1 = historialService.registrarCambio(null, "Registrado", "En pruebas", "admin");
        assertThat(r1).isNull();

        Historial r2 = historialService.registrarCambio(1L, "Registrado", null, "admin");
        assertThat(r2).isNull();

        Historial r3 = historialService.registrarCambio(1L, "Registrado", "   ", "admin");
        assertThat(r3).isNull();

        verifyNoInteractions(historialRepository);
    }

    @Test
    void obtenerHistorialPorCandidato_retornaLista() {
        Historial h1 = Historial.builder()
                .id(1L)
                .candidatoId(20L)
                .fecha(LocalDateTime.now())
                .estadoAnterior("Registrado")
                .estadoNuevo("En pruebas")
                .responsable("rrhh@flowmatic.com")
                .build();

        when(historialRepository.findByCandidatoIdOrderByFechaDesc(20L)).thenReturn(List.of(h1));

        List<Historial> lista = historialService.obtenerHistorialPorCandidato(20L);

        assertThat(lista).hasSize(1);
        assertThat(lista.get(0).getEstadoNuevo()).isEqualTo("En pruebas");
    }

    @Test
    void obtenerHistorialPorCandidato_idNull_retornaListaVacia() {
        List<Historial> lista = historialService.obtenerHistorialPorCandidato(null);
        assertThat(lista).isEmpty();
        verifyNoInteractions(historialRepository);
    }
}
