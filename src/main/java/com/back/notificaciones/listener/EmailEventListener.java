package com.back.notificaciones.listener;

import com.back.auth.event.PasswordResetSolicitadoEvent;
import com.back.auth.event.UsuarioRegistradoEvent;
import com.back.notificaciones.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailEventListener {

    private final EmailService emailService;

    @ApplicationModuleListener
    public void handleUsuarioRegistrado(UsuarioRegistradoEvent event) {
        if (event.getTokenActivacion() != null) {
            String nombre = event.getUsername();
            if (nombre == null || nombre.isEmpty()) {
                nombre = event.getEmail().substring(0, event.getEmail().indexOf("@"));
            }
            emailService.enviarEmailVerificacion(event.getEmail(), nombre, event.getTokenActivacion());
        }
    }

    @ApplicationModuleListener
    public void handlePasswordResetSolicitado(PasswordResetSolicitadoEvent event) {
        emailService.enviarEmailRecuperacion(event.getEmail(), event.getNombre(), event.getTokenReset());
    }
}
