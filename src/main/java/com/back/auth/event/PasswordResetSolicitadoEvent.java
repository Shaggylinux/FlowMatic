package com.back.auth.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PasswordResetSolicitadoEvent extends ApplicationEvent {
    private final String email;
    private final String nombre;
    private final String tokenReset;

    public PasswordResetSolicitadoEvent(Object source, String email, String nombre, String tokenReset) {
        super(source);
        this.email = email;
        this.nombre = nombre;
        this.tokenReset = tokenReset;
    }
}
