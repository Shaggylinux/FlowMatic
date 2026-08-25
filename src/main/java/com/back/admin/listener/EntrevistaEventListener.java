package com.back.admin.listener;

import com.back.admin.RRHH;
import com.back.admin.RRHHService;
import com.back.notificaciones.EmailService;
import com.back.notificaciones.NotificacionService;
import com.back.shared.event.AccionCandidatoEntrevistaEvent;
import com.back.shared.event.EntrevistaAgendadaEvent;
import com.back.shared.event.EntrevistaNotificacionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EntrevistaEventListener {

    private static final Logger logger = LoggerFactory.getLogger(EntrevistaEventListener.class);

    private final RRHHService rrhhService;
    private final EmailService emailService;
    private final NotificacionService notificacionService;

    @EventListener
    public void onEntrevistaAgendada(EntrevistaAgendadaEvent event) {
        try {
            String rrhhNombre = event.rrhhEmail();
            if (event.rrhhId() != null) {
                RRHH rrhhProfile = rrhhService.buscarPorId(event.rrhhId()).orElse(null);
                if (rrhhProfile != null) {
                    rrhhNombre = rrhhProfile.getUsername() + " " + (rrhhProfile.getApellido() != null ? rrhhProfile.getApellido() : "");
                }
            }

            emailService.enviarEmailEntrevista(event.rrhhEmail(), rrhhNombre, event.eventoDto(), event.candidatoNombre());

            if (event.candidatoEmail() != null && !event.candidatoEmail().isBlank()) {
                try {
                    emailService.enviarEmailEntrevistaCandidato(event.candidatoEmail(), event.candidatoNombre(), event.eventoDto());
                } catch (Exception candidatoEx) {
                    logger.warn("No se pudo enviar el email al candidato {}: {}", event.candidatoEmail(), candidatoEx.getMessage());
                }
            }

            String mensaje = "Entrevista agendada: " + event.candidatoNombre() + " — " + event.tipo() + " el " + event.fechaStr();
            notificacionService.crear("ENTREVISTA", mensaje, event.candidatoId(), event.candidatoNombre(), "/calendario");
            
            logger.info("Notificaciones de entrevista agendada enviadas correctamente.");
        } catch (Exception e) {
            logger.warn("No se pudieron procesar las notificaciones de entrevista agendada: {}", e.getMessage());
        }
    }

    @EventListener
    public void onEntrevistaNotificacion(EntrevistaNotificacionEvent event) {
        try {
            notificacionService.crear("ENTREVISTA", event.mensajeDetalle(), event.candidatoId(), event.candidatoNombre(), "/calendario");
            logger.info("Notificación web de entrevista enviada correctamente.");
        } catch (Exception e) {
            logger.warn("No se pudo enviar notificación web de entrevista: {}", e.getMessage());
        }
    }

    @EventListener
    public void onAccionCandidatoEntrevista(AccionCandidatoEntrevistaEvent event) {
        try {
            if (event.rrhhEmail() == null || event.rrhhEmail().isBlank()) {
                logger.warn("No se pudo notificar a RRHH: correo desconocido (rrhhId={})", event.rrhhId());
                return;
            }
            String rrhhNombre = event.rrhhEmail();
            if (event.rrhhId() != null) {
                RRHH rrhhProfile = rrhhService.buscarPorId(event.rrhhId()).orElse(null);
                if (rrhhProfile != null) {
                    rrhhNombre = rrhhProfile.getUsername() + " " + (rrhhProfile.getApellido() != null ? rrhhProfile.getApellido() : "");
                }
            }
            emailService.enviarEmailAccionCandidato(event.rrhhEmail(), rrhhNombre, event.accion(),
                    event.candidatoNombre(), event.fecha(), event.hora(),
                    event.nuevaFecha(), event.nuevaHora(), event.motivo());
            logger.info("Email de acción del candidato enviado a RRHH correctamente.");
        } catch (Exception e) {
            logger.warn("No se pudo enviar email de acción del candidato a RRHH: {}", e.getMessage());
        }
    }
}
