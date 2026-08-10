package com.back.calendario;

import com.back.auth.Usuario;
import com.back.auth.UsuarioRepository;
import com.back.candidatos.CandidatoService;
import com.back.exportacion.ExcelService;
import com.back.shared.dto.EntrevistaEmailDTO;
import com.back.shared.event.EntrevistaAgendadaEvent;
import com.back.shared.event.EntrevistaNotificacionEvent;
import org.springframework.http.ResponseEntity;
import java.util.stream.Collectors;
import org.springframework.context.ApplicationEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

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
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/calendario")
@RequiredArgsConstructor
public class CalendarioController {

    private static final Logger logger = LoggerFactory.getLogger(CalendarioController.class);

    private final EventoService eventoService;
    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CandidatoService candidatoService;
    private final ApplicationEventPublisher eventPublisher;
    private final ExcelService excelService;

    @GetMapping
    public String verCalendario(Model model, Principal principal) {
        model.addAttribute("candidatos", candidatoService.getSimpleList());

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
                    bgColor = "#DCFCE7";
                    borderColor = "#22C55E";
                    textColor = "#166534";
                }
                case "REPROGRAMADO" -> {
                    bgColor = "#FFEDD5";
                    borderColor = "#F97316";
                    textColor = "#9A3412";
                }
                case "CANCELADO" -> {
                    bgColor = "#FEE2E2";
                    borderColor = "#EF4444";
                    textColor = "#991B1B";
                }
                case "REALIZADA" -> {
                    bgColor = "#F1F5F9";
                    borderColor = "#94A3B8";
                    textColor = "#475569";
                }
                default -> {
                    bgColor = "#FEF9C3";
                    borderColor = "#EAB308";
                    textColor = "#854D0E";
                }
            }

            return new EventoCalendarioDTO(
                    e.getId(),
                    e.getCandidatoNombre() + " — " + e.getHora().toString(),
                    e.getFecha().toString() + "T" + e.getHora().toString(),
                    bgColor, borderColor, textColor,
                    props);
        }).toList();
    }

    private Usuario obtenerUsuario(Principal principal) {
        if (principal == null)
            return null;
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
            String candidatoNombre = candidatoService.getNombreCompleto(candidatoId);

            Evento evento = eventoService.crearEvento(candidatoId, candidatoNombre, fecha, hora, tipo, lugar, vacante, modalidad,
                    entrevistador, observaciones, estado, rrhh.getId());
            response.put("success", true);
            response.put("eventoId", evento.getId());

            try {
                EntrevistaEmailDTO eventoDto = new EntrevistaEmailDTO(evento.getFecha(), evento.getHora(), evento.getTipo(), evento.getLugar(), evento.getObservaciones());
                eventPublisher.publishEvent(new EntrevistaAgendadaEvent(
                        evento.getId(), candidatoId, candidatoNombre,
                        usuarioRepository.findById(candidatoId).map(Usuario::getEmail).orElse(null),
                        rrhh.getId(), rrhh.getEmail(), eventoDto,
                        tipo != null ? tipo : "Entrevista", fecha.toString()));
            } catch (Exception emailEx) {
                logger.warn("No se pudo publicar el evento de entrevista agendada: {}", emailEx.getMessage());
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
            eventoService.actualizarEvento(id, fecha, hora, tipo, lugar, vacante, modalidad, entrevistador,
                    observaciones);
            response.put("success", true);

            try {
                Evento evento = eventoService.buscarPorId(id);
                if (evento != null) {
                    eventPublisher.publishEvent(new EntrevistaNotificacionEvent(
                            evento.getCandidatoId(), evento.getCandidatoNombre(), "REPROGRAMACION",
                            "Entrevista reprogramada: " + evento.getCandidatoNombre() + " \u2014 "
                                    + (tipo != null ? tipo : "Entrevista") + " el " + fecha.toString()
                    ));
                }
            } catch (Exception notifEx) {
                logger.warn("No se pudo publicar notificaci\u00f3n de reprogramaci\u00f3n: {}", notifEx.getMessage());
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
                    "estado", ev.getEstado()));

            if ("ROLE_CANDIDATO".equals(user.getRol())) {
                try {
                    String candidatoNombre = ev.getCandidatoNombre() != null ? ev.getCandidatoNombre() : "Candidato";
                    String mensaje = "El candidato " + candidatoNombre +
                            " ha actualizado el estado de la entrevista del " + ev.getFecha() +
                            " a las " + ev.getHora() + " a \"" + estado + "\".";

                    if (ev.getRrhhId() != null) {
                        eventPublisher.publishEvent(new EntrevistaNotificacionEvent(
                            ev.getCandidatoId(), candidatoNombre, "ESTADO_CAMBIADO", mensaje
                        ));
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

        if (start == null)
            start = LocalDate.now().withDayOfMonth(1);
        if (end == null)
            end = start.withDayOfMonth(start.lengthOfMonth());

        List<Evento> eventos = eventoService.obtenerEventosFiltrados(start, end, candidatoId, estado, rrhhId);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=entrevistas.xlsx");

        String[] cabeceras = {"Fecha", "Hora", "Candidato", "Vacante", "Tipo", "Modalidad", "Ubicaci\u00f3n", "Entrevistador", "Estado"};
        List<Object[]> datos = eventos.stream()
            .map(e -> new Object[]{
                e.getFecha() != null ? e.getFecha().toString() : "",
                e.getHora() != null ? e.getHora().toString() : "",
                e.getCandidatoNombre() != null ? e.getCandidatoNombre() : "",
                e.getVacante() != null ? e.getVacante() : "",
                e.getTipo() != null ? e.getTipo() : "",
                e.getModalidad() != null ? e.getModalidad() : "",
                e.getLugar() != null ? e.getLugar() : "",
                e.getEntrevistador() != null ? e.getEntrevistador() : "",
                e.getEstado() != null ? e.getEstado() : ""
            }).toList();
        excelService.exportarDatos("Entrevistas", cabeceras, datos, response);
    }

    @GetMapping("/candidato/{id}/eventos")
    @ResponseBody
    public ResponseEntity<?> eventosCandidato(@PathVariable Long id) {
        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        if (usuario == null)
            return ResponseEntity.notFound().build();
        List<Evento> eventos = eventoRepository.findByCandidatoIdOrderByFechaDescHoraDesc(id);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("es"));
        List<EventoCandidatoDTO> list = eventos.stream().map(e -> {
            String tipo = e.getTipo();
            String color;
            if (tipo == null)
                color = "#6366F1";
            else
                switch (tipo) {
                    case "Entrevista RRHH" -> color = "#0EA5E9";
                    case "Entrevista Técnica" -> color = "#8B5CF6";
                    case "Reunión" -> color = "#F59E0B";
                    default -> color = "#6366F1";
                }
            return new EventoCandidatoDTO(
                    e.getId(),
                    tipo != null ? tipo : "Entrevista",
                    e.getFecha() != null ? e.getFecha().format(fmt) : "",
                    e.getHora() != null ? e.getHora().toString() : "",
                    e.getEstado() != null ? e.getEstado() : "",
                    tipo != null ? tipo : "",
                    e.getLugar() != null ? e.getLugar() : "",
                    e.getObservaciones() != null ? e.getObservaciones() : "",
                    color);
        }).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }
}
