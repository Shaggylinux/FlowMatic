package com.back.notificaciones.listener;

import com.back.auth.event.PasswordResetSolicitadoEvent;
import com.back.auth.event.UsuarioRegistradoEvent;
import com.back.notificaciones.EmailService;
import com.back.shared.event.CuentaBloqueadaEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailEventListener {

    private static final Logger logger = LoggerFactory.getLogger(EmailEventListener.class);
    private final EmailService emailService;

    @EventListener
    public void handleUsuarioRegistrado(UsuarioRegistradoEvent event) {
        try {
            String nombre = event.username();
            if (nombre == null || nombre.isEmpty()) {
                nombre = event.email().substring(0, event.email().indexOf("@"));
            }
            if ("ROLE_RRHH".equals(event.rol())) {
                emailService.enviarEmailBienvenidaRRHH(event.email(), nombre, event.apellido(), event.clavePlana(), event.tokenActivacion());
            } else {
                if (event.tokenActivacion() != null) {
                    emailService.enviarEmailVerificacion(event.email(), nombre, event.clavePlana(), event.tokenActivacion());
                }
            }
        } catch (Exception e) {
            logger.error("Error al procesar notificación por email de registro para {}: {}", event.email(), e.getMessage());
        }
    }

    @EventListener
    public void handlePasswordResetSolicitado(PasswordResetSolicitadoEvent event) {
        try {
            emailService.enviarEmailRecuperacion(event.email(), event.nombre(), event.tokenReset());
        } catch (Exception e) {
            logger.error("Error al procesar notificación por email de recuperación para {}: {}", event.email(), e.getMessage());
        }
    }

    @EventListener
    public void handleCuentaBloqueada(CuentaBloqueadaEvent event) {
        try {
            emailService.enviarEmailBloqueo(event.email(), event.nombre(), event.minutos());
        } catch (Exception e) {
            logger.error("Error al procesar notificación por email de bloqueo para {}: {}", event.email(), e.getMessage());
        }
    }
}
