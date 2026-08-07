package com.back.notificaciones.listener;

import com.back.auth.event.PasswordResetSolicitadoEvent;
import com.back.auth.event.UsuarioRegistradoEvent;
import com.back.notificaciones.EmailService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailEventListener {

    private static final Logger logger = LoggerFactory.getLogger(EmailEventListener.class);

    private final EmailService emailService;

    @ApplicationModuleListener
    public void handleUsuarioRegistrado(UsuarioRegistradoEvent event) {
        if (event.tokenActivacion() != null) {
            String nombre = event.username();
            if (nombre == null || nombre.isEmpty()) {
                nombre = event.email().substring(0, event.email().indexOf("@"));
            }
            boolean enviado = emailService.enviarEmailVerificacion(event.email(), nombre, event.tokenActivacion());
            if (!enviado) {
                logger.error("MAIL_VERIFICACION_FALLIDO -> destinatario={}. Revisar credenciales SMTP, red y configuracion de correo (application.properties / docker-compose).", event.email());
            }
        }
    }

    @ApplicationModuleListener
    public void handlePasswordResetSolicitado(PasswordResetSolicitadoEvent event) {
        boolean enviado = emailService.enviarEmailRecuperacion(event.email(), event.nombre(), event.tokenReset());
        if (!enviado) {
            logger.error("MAIL_RECUPERACION_FALLIDO -> destinatario={}. Revisar credenciales SMTP, red y configuracion de correo (application.properties / docker-compose).", event.email());
        }
    }
}
