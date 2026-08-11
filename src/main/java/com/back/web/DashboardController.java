package com.back.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.back.auth.Usuario;
import com.back.candidatos.Candidato;
import com.back.auth.UsuarioRepository;
import com.back.calendario.EventoRepository;
import com.back.calendario.Evento;
import com.back.notificaciones.Notificacion;
import com.back.candidatos.CandidatoRepository;
import com.back.notificaciones.NotificacionService;
import com.back.drive.Archivos;
import com.back.drive.ArchivosRepository;
import org.springframework.ui.Model;

import java.security.Principal;
import java.util.*;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final UsuarioRepository usuarioRepository;
    private final CandidatoRepository candidatoRepository;
    private final NotificacionService notificacionService;
    private final EventoRepository eventoRepository;
    private final ArchivosRepository filesRepository;

    @GetMapping
    public String mostrarDashboard(Principal principal, Model model) {
        String loginId = (principal != null) ? principal.getName() : null;
        if (loginId == null)
            return "redirect:/login";

        Usuario usuarioActual = usuarioRepository.findByEmail(loginId).orElse(null);
        if (usuarioActual == null || !"ROLE_RRHH".equals(usuarioActual.getRol())) {
            return "redirect:/"; // Solo RRHH puede ver el dashboard
        }

        Map<String, Object> usuarioData = new HashMap<>();
        usuarioData.put("id", usuarioActual.getId());
        usuarioData.put("email", usuarioActual.getEmail());
        usuarioData.put("rol", usuarioActual.getRol());
        usuarioData.put("activo", usuarioActual.isActivo());

        model.addAttribute("usuarioActualObjeto", usuarioData);
        model.addAttribute("usuarioActual", loginId);

        List<Archivos> todos = filesRepository.findAll();
        List<Archivos> carpetas = todos.stream()
                .filter(Archivos::isEsCarpeta)
                .filter(a -> !a.getNombre().contains("@"))
                .toList();
        List<Archivos> archivos = todos.stream()
                .filter(a -> !a.isEsCarpeta())
                .toList();
        
        model.addAttribute("carpetas", carpetas);
        model.addAttribute("archivos", archivos);

        List<Candidato> candidatoList = candidatoRepository.findAll();
        List<Map<String, Object>> candidatosConEmail = new ArrayList<>();
        Map<String, Integer> pipelineCounts = new HashMap<>();
        pipelineCounts.put("Registrado", 0);
        pipelineCounts.put("En pruebas", 0);
        pipelineCounts.put("Entrevista", 0);
        pipelineCounts.put("Contratado", 0);
        pipelineCounts.put("No aceptado", 0);

        for (Candidato c : candidatoList) {
            Map<String, Object> cm = new HashMap<>();
            cm.put("id", c.getId());
            cm.put("username", c.getUsername());
            cm.put("apellido", c.getApellido());
            String estadoStr = c.getEstado() != null ? c.getEstado() : "Registrado";
            cm.put("estado", estadoStr);
            usuarioRepository.findById(c.getId()).ifPresent(u -> cm.put("email", u.getEmail()));
            candidatosConEmail.add(cm);

            // Contador pipeline
            if (pipelineCounts.containsKey(estadoStr)) {
                pipelineCounts.put(estadoStr, pipelineCounts.get(estadoStr) + 1);
            }
        }
        model.addAttribute("listaCandidatos", candidatosConEmail);
        model.addAttribute("pipelineCounts", pipelineCounts);

        List<Notificacion> actividadReciente = notificacionService.obtenerActividadReciente();
        model.addAttribute("actividadReciente", actividadReciente);
        
        long notificacionesNoLeidas = notificacionService.contarNoLeidas();
        model.addAttribute("notificacionesNoLeidas", notificacionesNoLeidas);
        
        List<Evento> proximasEntrevistas = eventoRepository.findTop5ByFechaGreaterThanEqualOrderByFechaAscHoraAsc(java.time.LocalDate.now());
        model.addAttribute("proximasEntrevistas", proximasEntrevistas);

        return "dashboard-rrhh";
    }
}
