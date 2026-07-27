package com.back.candidatos;

import com.back.auth.UsuarioRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CandidatoService {

    private final CandidatoRepository candidatoRepository;
    private final UsuarioRepository usuarioRepository;

    private static final List<String> ESTADOS_EN_PROCESO = Arrays.asList(
        "Disponible", "En proceso", "Entrevista", "Pendiente", "Registrado"
    );

    public long contarActivos() {
        return usuarioRepository.countByRolAndActivo("ROLE_CANDIDATO", true);
    }

    public long contarNuevos() {
        LocalDateTime since = LocalDateTime.now().minusDays(30);
        return candidatoRepository.countByUltimaActualizacionAfter(since);
    }

    public long contarEnProceso() {
        return candidatoRepository.countByEstadoIn(ESTADOS_EN_PROCESO);
    }

    public long contarContratables() {
        LocalDateTime since = LocalDateTime.now().minusDays(90);
        List<Candidato> candidatos = candidatoRepository.findAll();
        return candidatos.stream()
            .filter(c -> c.getUltimaActualizacion() != null && c.getUltimaActualizacion().isAfter(since))
            .filter(c -> c.getCargo() != null && !c.getCargo().isBlank())
            .filter(c -> c.getTecnologias() != null && !c.getTecnologias().isBlank())
            .count();
    }

    public Map<String, Long> getComparativaSemanal() {
        Map<String, Long> comp = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekAgo = now.minusDays(7);
        LocalDateTime twoWeeksAgo = now.minusDays(14);

        long actual = candidatoRepository.countByUltimaActualizacionAfter(weekAgo);
        long anterior = candidatoRepository.countByUltimaActualizacionAfter(twoWeeksAgo);

        comp.put("nuevosActual", actual);
        comp.put("nuevosAnterior", anterior);
        return comp;
    }

    public Page<Candidato> listarCandidatos(String search, String cargo, String estado,
                                             String experiencia, String ciudad, int page, int size) {
        Integer expMin = null;
        if (experiencia != null && !experiencia.isBlank()) {
            try { expMin = Integer.parseInt(experiencia); } catch (NumberFormatException ignored) {}
        }

        String searchVal = (search != null && !search.isBlank()) ? "%" + search + "%" : null;
        String cargoVal = (cargo != null && !cargo.isBlank()) ? "%" + cargo + "%" : null;
        String estadoVal = (estado != null && !estado.isBlank()) ? estado : null;
        String ciudadVal = (ciudad != null && !ciudad.isBlank()) ? "%" + ciudad + "%" : null;

        Pageable pageable = PageRequest.of(page, size, Sort.unsorted());
        return candidatoRepository.findFiltrados(searchVal, cargoVal, estadoVal, expMin, ciudadVal, pageable);
    }


    public List<String> getCargos() {
        return candidatoRepository.findDistinctCargos();
    }

    public List<String> getCiudades() {
        return candidatoRepository.findDistinctCiudades();
    }

    public List<Candidato> listarCandidatosSinPaginar(String search, String estado) {
        String searchVal = (search != null && !search.isBlank()) ? "%" + search + "%" : null;
        String estadoVal = (estado != null && !estado.isBlank()) ? estado : null;
        return candidatoRepository.findFiltradosSinPaginar(searchVal, estadoVal);
    }

    public String getNombreCompleto(Long id) {
        return candidatoRepository.findById(id)
                .map(c -> c.getUsername() + " " + (c.getApellido() != null ? c.getApellido() : ""))
                .orElse("Candidato");
    }

    public List<Map<String, Object>> getSimpleList() {
        return candidatoRepository.findAll().stream().map(c -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", c.getId());
            m.put("nombre", c.getUsername() + " " + (c.getApellido() != null ? c.getApellido() : ""));
            return m;
        }).collect(Collectors.toList());
    }
}
