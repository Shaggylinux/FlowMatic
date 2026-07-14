package com.back.service;

import com.back.model.Usuario;
import com.back.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CandidatoService {

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
        return usuarioRepository.countByRolAndUltimaActualizacionAfter("ROLE_CANDIDATO", since);
    }

    public long contarEnProceso() {
        return usuarioRepository.countByRolAndEstadoIn("ROLE_CANDIDATO", ESTADOS_EN_PROCESO);
    }

    public long contarContratables() {
        LocalDateTime since = LocalDateTime.now().minusDays(90);
        List<Usuario> candidatos = usuarioRepository.findByRol("ROLE_CANDIDATO");
        return candidatos.stream()
            .filter(u -> u.getUltimaActualizacion() != null && u.getUltimaActualizacion().isAfter(since))
            .filter(u -> u.getCargo() != null && !u.getCargo().isBlank())
            .filter(u -> u.getTecnologias() != null && !u.getTecnologias().isBlank())
            .count();
    }

    public Map<String, Long> getComparativaSemanal() {
        Map<String, Long> comp = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekAgo = now.minusDays(7);
        LocalDateTime twoWeeksAgo = now.minusDays(14);

        long actual = usuarioRepository.countByRolAndUltimaActualizacionAfter("ROLE_CANDIDATO", weekAgo);
        long anterior = usuarioRepository.countByRolAndUltimaActualizacionAfter("ROLE_CANDIDATO", twoWeeksAgo);

        comp.put("nuevosActual", actual);
        comp.put("nuevosAnterior", anterior);
        return comp;
    }

    public Page<Usuario> listarCandidatos(String search, String cargo, String estado,
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
        return usuarioRepository.findCandidatosFiltrados(searchVal, cargoVal, estadoVal, expMin, ciudadVal, pageable);
    }

    public int calcularMatchScore(Usuario candidato) {
        int score = 0;

        if (candidato.getCargo() != null && !candidato.getCargo().isBlank()) score += 5;
        if (candidato.getCiudad() != null && !candidato.getCiudad().isBlank()) score += 5;
        if (candidato.getLinkedin() != null && !candidato.getLinkedin().isBlank()) score += 5;
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
            else if (disp.contains("semana") || disp.contains("día")) score += 10;
            else score += 5;
        }

        return Math.min(score, 100);
    }

    public String getMatchLabel(int score) {
        if (score >= 80) return "Excelente perfil";
        if (score >= 60) return "Buena coincidencia";
        if (score >= 40) return "Perfil en desarrollo";
        return "Perfil básico";
    }

    public List<String> getCargos() {
        return usuarioRepository.findDistinctCargosByRolCandidato();
    }

    public List<String> getCiudades() {
        return usuarioRepository.findDistinctCiudadesByRolCandidato();
    }

    public List<Usuario> listarCandidatosSinPaginar(String search, String estado) {
        String searchVal = (search != null && !search.isBlank()) ? "%" + search + "%" : null;
        String estadoVal = (estado != null && !estado.isBlank()) ? estado : null;
        return usuarioRepository.findCandidatosFiltradosSinPaginar(searchVal, estadoVal);
    }
}
