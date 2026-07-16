package com.back.service;

import com.back.model.Candidato;
import com.back.repository.CandidatoRepository;
import com.back.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

@Service
public class CandidatoService {

    @Autowired
    private CandidatoRepository candidatoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

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

    public int calcularMatchScore(Candidato candidato) {
        int score = 0;

        if (candidato.getCargo() != null && !candidato.getCargo().isBlank()) score += 5;
        if (candidato.getCiudad() != null && !candidato.getCiudad().isBlank()) score += 5;
        if (candidato.getTelefono() != null && !candidato.getTelefono().isBlank()) score += 5;

        if (candidato.getExperiencia() != null && candidato.getExperiencia() > 0) {
            if (candidato.getExperiencia() >= 5) score += 20;
            else if (candidato.getExperiencia() >= 2) score += 15;
            else score += 10;
        }

        if (candidato.getTecnologias() != null && !candidato.getTecnologias().isBlank()) {
            String[] tecs = candidato.getTecnologias().split(",");
            if (tecs.length >= 5) score += 30;
            else if (tecs.length >= 3) score += 20;
            else score += 15;
        }

        if (candidato.getIdiomas() != null && !candidato.getIdiomas().isBlank()) {
            String[] langs = candidato.getIdiomas().split(",");
            if (langs.length >= 2) score += 15;
            else score += 10;
        }

        if (candidato.getDisponibilidad() != null && !candidato.getDisponibilidad().isBlank()) {
            String disp = candidato.getDisponibilidad().toLowerCase();
            if (disp.contains("inmediata")) score += 15;
            else if (disp.contains("semana") || disp.contains("d\u00eda")) score += 10;
            else score += 5;
        }

        return Math.min(score, 100);
    }

    public String getMatchLabel(int score) {
        if (score >= 80) return "Excelente perfil";
        if (score >= 60) return "Buena coincidencia";
        if (score >= 40) return "Perfil en desarrollo";
        return "Perfil b\u00e1sico";
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
}
