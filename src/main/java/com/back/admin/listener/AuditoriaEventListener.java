package com.back.admin.listener;

import com.back.admin.AuditoriaService;
import com.back.shared.event.AuditoriaEvent;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuditoriaEventListener {

    private final AuditoriaService auditoriaService;

    @ApplicationModuleListener
    public void on(AuditoriaEvent event) {
        auditoriaService.registrar(event.getModulo(), event.getAccion(), event.getUsuarioEmail(), event.getIp());
    }
}
