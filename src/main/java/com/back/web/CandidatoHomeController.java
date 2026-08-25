package com.back.web;

import com.back.auth.Usuario;
import com.back.auth.UsuarioService;
import com.back.calendario.Evento;
import com.back.calendario.EventoService;
import com.back.candidatos.Candidato;
import com.back.candidatos.CandidatoService;
import com.back.drive.Archivos;
import com.back.drive.FilesServices;
import com.back.notificaciones.Notificacion;
import com.back.notificaciones.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class CandidatoHomeController {

    private final UsuarioService usuarioService;
    private final CandidatoService candidatoService;
    private final EventoService eventoService;
    private final FilesServices filesServices;
    private final NotificacionService notificacionService;
    private final com.back.shared.HistorialService historialService;

    @GetMapping("/candidato/home")
    public String vistaCandidato(Authentication auth, Model model) {
        String email = auth != null ? auth.getName() : null;
        String primerNombre = "";
        String nombreCompleto = "";
        String iniciales = "";
        String estado = "Registrado";
        String cargo = "";
        String ciudad = "";
        Long candidatoId = null;
        String ultimaActualizacionFecha = "Reciente";
        String ultimaActualizacionMensaje = "Sin actualizaciones registradas";

        if (email != null) {
            Optional<Usuario> uOpt = usuarioService.buscarPorEmail(email);
            if (uOpt.isPresent()) {
                Usuario u = uOpt.get();
                candidatoId = u.getId();
                Optional<Candidato> cOpt = candidatoService.buscarPorId(u.getId());
                if (cOpt.isPresent()) {
                    Candidato c = cOpt.get();
                    if (c.getUsername() != null && !c.getUsername().isBlank()) {
                        primerNombre = c.getUsername().split(" ")[0];
                        nombreCompleto = c.getUsername() + (c.getApellido() != null && !c.getApellido().isBlank() ? " " + c.getApellido() : "");

                        String n1 = c.getUsername().substring(0, 1).toUpperCase();
                        String a1 = (c.getApellido() != null && !c.getApellido().isBlank()) ? c.getApellido().substring(0, 1).toUpperCase() : "";
                        iniciales = n1 + a1;
                    } else {
                        primerNombre = email.split("@")[0];
                        nombreCompleto = email;
                        iniciales = primerNombre.substring(0, 1).toUpperCase();
                    }

                    if (c.getEstado() != null && !c.getEstado().isBlank()) {
                        estado = c.getEstado();
                    }
                    if (c.getCargo() != null && !c.getCargo().isBlank()) {
                        cargo = c.getCargo();
                    }
                    if (c.getCiudad() != null && !c.getCiudad().isBlank()) {
                        ciudad = c.getCiudad();
                    }
                    if (c.getUltimaActualizacion() != null) {
                        ultimaActualizacionFecha = c.getUltimaActualizacion().format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
                        ultimaActualizacionMensaje = "Tu expediente fue actualizado en el sistema.";
                    }
                } else {
                    primerNombre = email.split("@")[0];
                    nombreCompleto = email;
                    iniciales = primerNombre.substring(0, 1).toUpperCase();
                }
            }
        }

        // Consultar entrevistas reales
        List<Evento> entrevistas = candidatoId != null ? eventoService.buscarPorCandidatoIdOrdenado(candidatoId) : Collections.emptyList();
        Evento proximaEntrevista = entrevistas.stream()
                .filter(e -> e.getFecha() != null && !e.getFecha().isBefore(LocalDate.now()))
                .findFirst()
                .orElse(null);

        // Consultar archivos / documentos reales
        List<Archivos> candidatosArchivos = email != null ? filesServices.buscarArchivosVisiblesPara(email) : Collections.emptyList();
        long archivosTotal = candidatosArchivos.stream().filter(a -> !a.isEsCarpeta()).count();

        // Consultar notificaciones reales
        List<Notificacion> notificaciones = candidatoId != null
                ? notificacionService.obtenerActividadReciente(candidatoId)
                : notificacionService.obtenerActividadReciente();
        long notificacionesNoLeidas = candidatoId != null
                ? notificacionService.contarNoLeidasPorCandidato(candidatoId)
                : notificacionService.contarNoLeidasGlobales();

        // Cálculo de etapa y porcentaje según estado real
        int stepIndex = 1;
        int porcentaje = 20;
        boolean rechazado = false;
        String estadoLower = estado.toLowerCase();
        if (estadoLower.contains("prueba")) {
            stepIndex = 2;
            porcentaje = 40;
        } else if (estadoLower.contains("técnica") || estadoLower.contains("tecnica") || estadoLower.contains("entrevista")) {
            stepIndex = 3;
            porcentaje = 60;
        } else if (estadoLower.contains("final")) {
            stepIndex = 4;
            porcentaje = 80;
        } else if (estadoLower.contains("rechazad") || estadoLower.contains("no aceptado") || estadoLower.contains("no seleccionado") || estadoLower.contains("descartado")) {
            rechazado = true;
            stepIndex = 0;
            porcentaje = 0;
        } else if (estadoLower.contains("contratado") || (estadoLower.contains("aceptado") && !estadoLower.contains("no aceptado")) || estadoLower.contains("realizada")) {
            stepIndex = 5;
            porcentaje = 100;
        }

        model.addAttribute("estadoRechazado", rechazado);

        model.addAttribute("candidatoId", candidatoId);
        model.addAttribute("candidatoPrimerNombre", !primerNombre.isBlank() ? primerNombre : "Candidato");
        model.addAttribute("candidatoNombreCompleto", !nombreCompleto.isBlank() ? nombreCompleto : "Candidato");
        model.addAttribute("usuarioIniciales", !iniciales.isBlank() ? iniciales : "C");
        model.addAttribute("candidatoEmail", email != null ? email : "");
        model.addAttribute("candidatoEstado", estado);
        model.addAttribute("candidatoCargo", cargo);
        model.addAttribute("candidatoCiudad", ciudad);
        model.addAttribute("proximasEntrevistas", entrevistas);
        model.addAttribute("proximaEntrevista", proximaEntrevista);
        model.addAttribute("candidatoArchivos", candidatosArchivos);
        model.addAttribute("archivosTotal", archivosTotal);
        model.addAttribute("notificaciones", notificaciones);
        model.addAttribute("notificacionesNoLeidas", notificacionesNoLeidas);
        model.addAttribute("stepIndex", stepIndex);
        model.addAttribute("procesoProgresoPct", porcentaje);
        List<com.back.shared.Historial> historialProceso = candidatoId != null
                ? historialService.obtenerHistorialPorCandidato(candidatoId)
                : Collections.emptyList();

        model.addAttribute("historialProceso", historialProceso);
        model.addAttribute("ultimaActualizacionFecha", ultimaActualizacionFecha);
        model.addAttribute("ultimaActualizacionMensaje", ultimaActualizacionMensaje);

        return "candidato";
    }
}
