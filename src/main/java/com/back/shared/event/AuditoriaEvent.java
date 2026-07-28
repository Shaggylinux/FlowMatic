package com.back.shared.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.context.ApplicationEvent;

public class AuditoriaEvent extends ApplicationEvent {
    private final String modulo;
    private final String accion;
    private final String usuarioEmail;
    private final String ip;

    @JsonCreator
    public AuditoriaEvent(
            @JsonProperty("source") Object source,
            @JsonProperty("modulo") String modulo,
            @JsonProperty("accion") String accion,
            @JsonProperty("usuarioEmail") String usuarioEmail,
            @JsonProperty("ip") String ip) {
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
