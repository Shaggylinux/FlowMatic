package com.back.candidatos;

import com.back.auth.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CandidatoServiceTest {

    @Mock
    private CandidatoRepository candidatoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;

    private CandidatoService service;

    @BeforeEach
    void setUp() {
        service = new CandidatoService(candidatoRepository, usuarioRepository);
    }

    @Test
    void listarCandidatos_armaLosParametrosConComodines() {
        when(candidatoRepository.findFiltrados(eq("%ana%"), eq("%java%"), eq("Entrevista"), eq(2), eq("%med%"), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));

        service.listarCandidatos("ana", "java", "Entrevista", "2", "med", 0, 10);

        verify(candidatoRepository).findFiltrados(eq("%ana%"), eq("%java%"), eq("Entrevista"), eq(2), eq("%med%"), any(PageRequest.class));
    }

    @Test
    void listarCandidatos_sinFiltrosPasaNull() {
        when(candidatoRepository.findFiltrados(isNull(), isNull(), isNull(), isNull(), isNull(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));

        service.listarCandidatos(null, "", "", null, "", 0, 10);

        verify(candidatoRepository).findFiltrados(isNull(), isNull(), isNull(), isNull(), isNull(), any(PageRequest.class));
    }

    @Test
    void listarCandidatos_experienciaNoNumericaSeIgnora() {
        when(candidatoRepository.findFiltrados(isNull(), isNull(), isNull(), isNull(), isNull(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));

        service.listarCandidatos(null, null, null, "abc", null, 0, 10);

        verify(candidatoRepository).findFiltrados(isNull(), isNull(), isNull(), isNull(), isNull(), any(PageRequest.class));
    }

    @Test
    void getNombreCompleto_candidatoInexistenteDevuelveCandidato() {
        when(candidatoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(service.getNombreCompleto(99L)).isEqualTo("Candidato");
    }

    @Test
    void getNombreCompleto_candidatoExistenteComponeNombre() {
        Candidato c = new Candidato();
        c.setId(1L);
        c.setUsername("Ana");
        c.setApellido("López");
        when(candidatoRepository.findById(1L)).thenReturn(Optional.of(c));

        assertThat(service.getNombreCompleto(1L)).isEqualTo("Ana López");
    }

    @Test
    void contarActivos_delegaEnRepositorio() {
        when(usuarioRepository.countByRolAndActivo("ROLE_CANDIDATO", true)).thenReturn(7L);

        assertThat(service.contarActivos()).isEqualTo(7L);
    }

    @Test
    void getSimpleList_devuelveIdYNombre() {
        Candidato c = new Candidato();
        c.setId(3L);
        c.setUsername("Pedro");
        c.setApellido("Gómez");
        when(candidatoRepository.findAll()).thenReturn(List.of(c));

        List<java.util.Map<String, Object>> lista = service.getSimpleList();

        assertThat(lista).hasSize(1);
        assertThat(lista.get(0)).containsEntry("id", 3L).containsEntry("nombre", "Pedro Gómez");
    }
}
