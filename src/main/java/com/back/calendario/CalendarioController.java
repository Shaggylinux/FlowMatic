package com.back.calendario;

import com.back.auth.Usuario;
import com.back.candidatos.Candidato;
import com.back.admin.RRHH;
import com.back.auth.UsuarioRepository;
import com.back.candidatos.CandidatoRepository;
import com.back.admin.RRHHRepository;
import com.back.shared.EmailService;
import com.back.shared.ExcelService;
import com.back.notificaciones.NotificacionService;
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
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequestMapping("/calendario")
public class CalendarioController {

    private static final Logger logger = LoggerFactory.getLogger(CalendarioController.class);

    @Autowired
    private EventoService eventoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CandidatoRepository candidatoRepository;

    @Autowired
    private RRHHRepository rrhhRepository;

    @Autowired
    private NotificacionService notificacionService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ExcelService excelService;

    @GetMapping
    public String verCalendario(Model model, Principal principal) {
        List<Candidato> candidatos = candidatoRepository.findAll();
        model.addAttribute("candidatos", candidatos);

        Usuario user = obtenerUsuario(principal);
        if (user != null) {
            model.addAttribute("rrhhId", user.getId());
            model.addAttribute("currentUserId", user.getId());
            model.addAttribute("currentUserRol", user.getRol());
        }

        LocalDate hoy = LocalDate.now();
        LocalDate ayer = hoy.minusDays(1);

        long totalHoy = eventoService.contarHoy();
        long totalPendientes = eventoService.contarPendientes();
        long totalConfirmadas = eventoService.contarConfirmadas();
        long totalReprogramadas = eventoService.contarReprogramadas();
        long totalCanceladas = eventoService.contarCanceladas();

        model.addAttribute("totalHoy", totalHoy);
        model.addAttribute("totalPendientes", totalPendientes);
        model.addAttribute("totalConfirmadas", totalConfirmadas);
        model.addAttribute("totalReprogramadas", totalReprogramadas);
        model.addAttribute("totalCanceladas", totalCanceladas);
        model.addAttribute("totalEsteMes", eventoService.contarTotalEsteMes());
        model.addAttribute("candidatosUnicosEsteMes", eventoService.contarCandidatosUnicosEsteMes());

        model.addAttribute("difHoy", totalHoy - eventoService.contarFecha(ayer));

        model.addAttribute("proximasEntrevistas", eventoService.obtenerProximasEntrevistas(10));

        return "calendario";
    }

    @GetMapping("/eventos")
    @ResponseBody
    public List<EventoCalendarioDTO> obtenerEventos(
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
            Map<String, Object> props = new HashMap<>();
            props.put("candidatoId", e.getCandidatoId());
            props.put("candidatoNombre", e.getCandidatoNombre());
            props.put("tipo", e.getTipo() != null ? e.getTipo() : "");
            props.put("estado", e.getEstado() != null ? e.getEstado() : "");
            props.put("lugar", e.getLugar() != null ? e.getLugar() : "");
            props.put("vacante", e.getVacante() != null ? e.getVacante() : "");
            props.put("modalidad", e.getModalidad() != null ? e.getModalidad() : "");
            props.put("entrevistador", e.getEntrevistador() != null ? e.getEntrevistador() : "");
            props.put("observaciones", e.getObservaciones() != null ? e.getObservaciones() : "");

            String estadoEvento = e.getEstado() != null ? e.getEstado() : "PENDIENTE";
            String bgColor, borderColor, textColor;
            switch (estadoEvento) {
                case "CONFIRMADO" -> {
                    bgColor = "#DCFCE7"; borderColor = "#22C55E"; textColor = "#166534";
                }
                case "REPROGRAMADO" -> {
                    bgColor = "#FFEDD5"; borderColor = "#F97316"; textColor = "#9A3412";
                }
                case "CANCELADO" -> {
                    bgColor = "#FEE2E2"; borderColor = "#EF4444"; textColor = "#991B1B";
                }
                default -> {
                    bgColor = "#FEF9C3"; borderColor = "#EAB308"; textColor = "#854D0E";
                }
            }

            return new EventoCalendarioDTO(
                e.getId(),
                e.getCandidatoNombre() + " — " + e.getHora().toString(),
                e.getFecha().toString() + "T" + e.getHora().toString(),
                bgColor, borderColor, textColor,
                props
            );
        }).toList();
    }

    private Usuario obtenerUsuario(Principal principal) {
        if (principal == null) return null;
        return usuarioRepository.findByEmail(principal.getName()).orElse(null);
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
            @RequestParam(required = false) String vacante,
            @RequestParam(required = false) String modalidad,
            @RequestParam(required = false) String entrevistador,
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
            Evento evento = eventoService.crearEvento(candidatoId, fecha, hora, tipo, lugar, vacante, modalidad, entrevistador, observaciones, estado, rrhh.getId());
            response.put("success", true);
            response.put("eventoId", evento.getId());

            try {
                Candidato candidato = candidatoRepository.findById(candidatoId).orElse(null);
                String candidatoNombre = candidato != null ? candidato.getUsername() + " " + candidato.getApellido() : "Candidato";
                String rrhhNombre = rrhh.getEmail();
                RRHH rrhhProfile = rrhhRepository.findById(rrhh.getId()).orElse(null);
                if (rrhhProfile != null) {
                    rrhhNombre = rrhhProfile.getUsername() + " " + rrhhProfile.getApellido();
                }
                emailService.enviarEmailEntrevista(rrhh.getEmail(), rrhhNombre, evento, candidatoNombre);

                notificacionService.crear("ENTREVISTA",
                    "Entrevista agendada: " + candidatoNombre + " — " + (tipo != null ? tipo : "Entrevista") + " el " + fecha.toString(),
                    candidatoId, candidatoNombre, "/calendario");
            } catch (Exception emailEx) {
                logger.warn("No se pudo enviar el email de confirmaci\u00f3n: {}", emailEx.getMessage());
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    @PostMapping("/actualizar/{id}")
    @ResponseBody
    public Map<String, Object> actualizarEvento(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime hora,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String lugar,
            @RequestParam(required = false) String vacante,
            @RequestParam(required = false) String modalidad,
            @RequestParam(required = false) String entrevistador,
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
            eventoService.actualizarEvento(id, fecha, hora, tipo, lugar, vacante, modalidad, entrevistador, observaciones);
            response.put("success", true);

            try {
                Evento evento = eventoService.buscarPorId(id);
                if (evento != null) {
                    notificacionService.crear("ENTREVISTA",
                        "Entrevista reprogramada: " + evento.getCandidatoNombre() + " \u2014 " + (tipo != null ? tipo : "Entrevista") + " el " + fecha.toString(),
                        evento.getCandidatoId(), evento.getCandidatoNombre(), "/calendario");
                }
            } catch (Exception notifEx) {
                logger.warn("No se pudo enviar notificaci\u00f3n de reprogramaci\u00f3n: {}", notifEx.getMessage());
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
            if (!"CONFIRMADO".equals(estado) && !"CANCELADO".equals(estado) && !"REPROGRAMADO".equals(estado)) {
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

            if ("ROLE_CANDIDATO".equals(user.getRol())) {
                try {
                    String candidatoNombre = ev.getCandidatoNombre() != null ? ev.getCandidatoNombre() : "Candidato";
                    String asunto = "El candidato " + candidatoNombre + " ha " +
                        ("CONFIRMADO".equals(estado) ? "confirmado" : "solicitado cambios en") +
                        " su entrevista";
                    String mensaje = "El candidato " + candidatoNombre +
                        " ha actualizado el estado de la entrevista del " + ev.getFecha() +
                        " a las " + ev.getHora() + " a \"" + estado + "\".";

                    if (ev.getRrhhId() != null) {
                        usuarioRepository.findById(ev.getRrhhId()).ifPresent(rrhh -> {
                            notificacionService.crear("ENTREVISTA", mensaje,
                                ev.getCandidatoId(), candidatoNombre, "/calendario");
                        });
                    }
                } catch (Exception notifEx) {
                    logger.warn("No se pudo notificar a RRHH: {}", notifEx.getMessage());
                }
            }
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

    @PostMapping("/{id}/nota")
    @ResponseBody
    public Map<String, Object> guardarNota(
            @PathVariable Long id,
            @RequestParam String observaciones,
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

        if ("ROLE_CANDIDATO".equals(user.getRol())) {
            if (!evento.getCandidatoId().equals(user.getId())) {
                response.put("success", false);
                response.put("error", "No autorizado");
                return response;
            }
        }

        try {
            eventoService.actualizarObservaciones(id, observaciones);
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    @GetMapping("/export")
    public void exportar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(required = false) Long candidatoId,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Long rrhhId,
            Principal principal,
            HttpServletResponse response) throws IOException {

        Usuario user = obtenerUsuario(principal);
        if (user == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        if ("ROLE_CANDIDATO".equals(user.getRol())) {
            candidatoId = user.getId();
        }

        if (start == null) start = LocalDate.now().withDayOfMonth(1);
        if (end == null) end = start.withDayOfMonth(start.lengthOfMonth());

        List<Evento> eventos = eventoService.obtenerEventosFiltrados(start, end, candidatoId, estado, rrhhId);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=entrevistas.xlsx");

        excelService.exportarEventos(eventos, response);
    }
}
