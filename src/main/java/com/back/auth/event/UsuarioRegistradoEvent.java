package com.back.auth.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UsuarioRegistradoEvent extends ApplicationEvent {
    private final Long usuarioId;
    private final String email;
    private final String rol;
    private final String username;
    private final String apellido;
    private final String telefono;

    public UsuarioRegistradoEvent(Object source, Long usuarioId, String email, String rol, String username, String apellido, String telefono) {
        super(source);
        this.usuarioId = usuarioId;
        this.email = email;
        this.rol = rol;
        this.username = username;
        this.apellido = apellido;
        this.telefono = telefono;
    }
}
