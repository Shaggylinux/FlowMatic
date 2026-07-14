package com.back.controller;

import com.back.model.Evento;
import com.back.model.Usuario;
import com.back.repository.UsuarioRepository;
import com.back.service.EmailService;
import com.back.service.EventoService;
import com.back.service.NotificacionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/calendario")
public class CalendarioController {

    private static final Logger logger = LoggerFactory.getLogger(CalendarioController.class);

    @Autowired
    private EventoService eventoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private NotificacionService notificacionService;

    @Autowired
    private EmailService emailService;

    @GetMapping
    public String verCalendario(Model model, Principal principal) {
        List<Usuario> candidatos = usuarioRepository.findByRol("ROLE_CANDIDATO");
        model.addAttribute("candidatos", candidatos);

        Usuario user = obtenerUsuario(principal);
        if (user != null) {
            model.addAttribute("rrhhId", user.getId());
            model.addAttribute("currentUserId", user.getId());
            model.addAttribute("currentUserRol", user.getRol());
        }

        model.addAttribute("totalHoy", eventoService.contarHoy());
        model.addAttribute("totalPendientes", eventoService.contarPendientes());
        model.addAttribute("totalConfirmadas", eventoService.contarConfirmadas());
        model.addAttribute("totalReprogramadas", eventoService.contarReprogramadas());
        model.addAttribute("totalCanceladas", eventoService.contarCanceladas());
        model.addAttribute("totalEsteMes", eventoService.contarTotalEsteMes());
        model.addAttribute("candidatosUnicosEsteMes", eventoService.contarCandidatosUnicosEsteMes());

        Optional<Evento> proxima = eventoService.obtenerProximaEntrevista();
        model.addAttribute("proximaEntrevista", proxima.orElse(null));

        return "calendario";
    }

    @GetMapping("/eventos")
    @ResponseBody
    public List<Map<String, Object>> obtenerEventos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(required = false) Long candidatoId,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Long rrhhId,
            Principal principal) {

        Usuario user = obtenerUsuario(principal);

        if (user != null && "ROLE_CANDIDATO".equals(user.getRol())) {
            candidatoId = user.getId();
        }

        List<Evento> eventos;
        if (candidatoId != null || estado != null || rrhhId != null) {
            eventos = eventoService.obtenerEventosFiltrados(start, end, candidatoId, estado, rrhhId);
        } else {
            eventos = eventoService.obtenerEventosEnRango(start, end);
        }

        return eventos.stream().map(e -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", e.getId());
            map.put("title", e.getCandidatoNombre() + " — " + e.getHora().toString());
            map.put("start", e.getFecha().toString() + "T" + e.getHora().toString());

            Map<String, Object> props = new HashMap<>();
            props.put("candidatoNombre", e.getCandidatoNombre());
            props.put("tipo", e.getTipo() != null ? e.getTipo() : "");
            props.put("estado", e.getEstado() != null ? e.getEstado() : "");
            props.put("lugar", e.getLugar() != null ? e.getLugar() : "");
            props.put("observaciones", e.getObservaciones() != null ? e.getObservaciones() : "");
            map.put("extendedProps", props);

            String estadoEvento = e.getEstado() != null ? e.getEstado() : "PENDIENTE";
            switch (estadoEvento) {
                case "CONFIRMADO" -> {
                    map.put("backgroundColor", "#DCFCE7");
                    map.put("borderColor", "#22C55E");
                    map.put("textColor", "#166534");
                }
                case "REPROGRAMADO" -> {
                    map.put("backgroundColor", "#FFEDD5");
                    map.put("borderColor", "#F97316");
                    map.put("textColor", "#9A3412");
                }
                case "CANCELADO" -> {
                    map.put("backgroundColor", "#FEE2E2");
                    map.put("borderColor", "#EF4444");
                    map.put("textColor", "#991B1B");
                }
                default -> {
                    map.put("backgroundColor", "#FEF9C3");
                    map.put("borderColor", "#EAB308");
                    map.put("textColor", "#854D0E");
                }
            }

            return map;
        }).toList();
    }

    private Usuario obtenerUsuario(Principal principal) {
        if (principal == null) return null;
        Usuario user = usuarioRepository.findByUsername(principal.getName());
        if (user == null) {
            user = usuarioRepository.findByEmail(principal.getName()).orElse(null);
        }
        return user;
    }

    @PostMapping("/crear")
    @ResponseBody
    public Map<String, Object> crearEvento(
            @RequestParam Long candidatoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime hora,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String lugar,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String observaciones,
            Principal principal) {

        Map<String, Object> response = new HashMap<>();
        Usuario rrhh = obtenerUsuario(principal);

        if (rrhh == null || !"ROLE_RRHH".equals(rrhh.getRol())) {
            response.put("success", false);
            response.put("error", "No autorizado");
            return response;
        }

        try {
            Evento evento = eventoService.crearEvento(candidatoId, fecha, hora, tipo, lugar, observaciones, estado, rrhh.getId());
            response.put("success", true);
            response.put("eventoId", evento.getId());

            try {
                Usuario candidato = usuarioRepository.findById(candidatoId).orElse(null);
                String candidatoNombre = candidato != null ? candidato.getUsername() + " " + candidato.getApellido() : "Candidato";
                emailService.enviarEmailEntrevista(rrhh.getEmail(), rrhh.getUsername(), evento, candidatoNombre);

                notificacionService.crear("ENTREVISTA",
                    "Entrevista agendada: " + candidatoNombre + " — " + (tipo != null ? tipo : "Entrevista") + " el " + fecha.toString(),
                    candidatoId, candidatoNombre, "/calendario");
            } catch (Exception emailEx) {
                logger.warn("No se pudo enviar el email de confirmación: {}", emailEx.getMessage());
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    @PostMapping("/cambiar-estado/{id}")
    @ResponseBody
    public Map<String, Object> cambiarEstado(
            @PathVariable Long id,
            @RequestParam String estado,
            Principal principal) {

        Map<String, Object> response = new HashMap<>();
        Usuario user = obtenerUsuario(principal);
        if (user == null) {
            response.put("success", false);
            response.put("error", "Usuario no encontrado");
            return response;
        }

        Evento evento = eventoService.buscarPorId(id);
        if (evento == null) {
            response.put("success", false);
            response.put("error", "Evento no encontrado");
            return response;
        }

        if ("ROLE_RRHH".equals(user.getRol())) {
            // RRHH puede cambiar a cualquier estado
        } else if ("ROLE_CANDIDATO".equals(user.getRol())) {
            if (!evento.getCandidatoId().equals(user.getId())) {
                response.put("success", false);
                response.put("error", "No autorizado");
                return response;
            }
            if (!"CONFIRMADO".equals(estado) && !"CANCELADO".equals(estado)) {
                response.put("success", false);
                response.put("error", "Solo puedes confirmar o cancelar la entrevista");
                return response;
            }
        } else {
            response.put("success", false);
            response.put("error", "No autorizado");
            return response;
        }

        try {
            Evento ev = eventoService.actualizarEstado(id, estado);
            response.put("success", true);
            response.put("evento", Map.of(
                "id", ev.getId(),
                "estado", ev.getEstado()
            ));
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    @PostMapping("/eliminar/{id}")
    @ResponseBody
    public Map<String, Object> eliminarEvento(@PathVariable Long id, Principal principal) {
        Map<String, Object> response = new HashMap<>();
        Usuario user = obtenerUsuario(principal);
        if (user == null || !"ROLE_RRHH".equals(user.getRol())) {
            response.put("success", false);
            response.put("error", "No autorizado");
            return response;
        }
        try {
            eventoService.eliminarEvento(id);
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }
}
