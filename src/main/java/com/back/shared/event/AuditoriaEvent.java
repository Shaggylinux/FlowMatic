package com.back.shared.event;

import org.springframework.context.ApplicationEvent;

public class AuditoriaEvent extends ApplicationEvent {
    private final String modulo;
    private final String accion;
    private final String usuarioEmail;
    private final String ip;

    public AuditoriaEvent(Object source, String modulo, String accion, String usuarioEmail, String ip) {
        super(source);
        this.modulo = modulo;
        this.accion = accion;
        this.usuarioEmail = usuarioEmail;
        this.ip = ip;
    }

    public String getModulo() { return modulo; }
    public String getAccion() { return accion; }
    public String getUsuarioEmail() { return usuarioEmail; }
    public String getIp() { return ip; }
}
