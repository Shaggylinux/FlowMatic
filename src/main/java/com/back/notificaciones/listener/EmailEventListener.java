package com.back.notificaciones.listener;

import com.back.auth.event.PasswordResetSolicitadoEvent;
import com.back.auth.event.UsuarioRegistradoEvent;
import com.back.notificaciones.EmailService;
import com.back.shared.event.CuentaBloqueadaEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailEventListener {

    private final EmailService emailService;

    @ApplicationModuleListener
    public void handleUsuarioRegistrado(UsuarioRegistradoEvent event) {
        if (event.tokenActivacion() != null) {
            String nombre = event.username();
            if (nombre == null || nombre.isEmpty()) {
                nombre = event.email().substring(0, event.email().indexOf("@"));
            }
            emailService.enviarEmailVerificacion(event.email(), nombre, event.tokenActivacion());
        }
    }

    @ApplicationModuleListener
    public void handlePasswordResetSolicitado(PasswordResetSolicitadoEvent event) {
        emailService.enviarEmailRecuperacion(event.email(), event.nombre(), event.tokenReset());
    }

    @ApplicationModuleListener
    public void handleCuentaBloqueada(CuentaBloqueadaEvent event) {
        emailService.enviarEmailBloqueo(event.email(), event.nombre(), event.minutos());
    }
}
