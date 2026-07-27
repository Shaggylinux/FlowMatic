package com.back.notificaciones.listener;

import com.back.auth.event.PasswordResetSolicitadoEvent;
import com.back.auth.event.UsuarioRegistradoEvent;
import com.back.notificaciones.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailEventListener {

    private final EmailService emailService;

    @Async
    @EventListener
    public void handleUsuarioRegistrado(UsuarioRegistradoEvent event) {
        if (event.getTokenActivacion() != null) {
            String nombre = event.getUsername();
            if (nombre == null || nombre.isEmpty()) {
                nombre = event.getEmail().substring(0, event.getEmail().indexOf("@"));
            }
            emailService.enviarEmailVerificacion(event.getEmail(), nombre, event.getTokenActivacion());
        }
    }

    @Async
    @EventListener
    public void handlePasswordResetSolicitado(PasswordResetSolicitadoEvent event) {
        emailService.enviarEmailRecuperacion(event.getEmail(), event.getNombre(), event.getTokenReset());
    }
}
