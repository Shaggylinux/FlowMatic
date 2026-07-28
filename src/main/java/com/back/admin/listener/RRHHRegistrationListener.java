package com.back.admin.listener;

import com.back.admin.RRHH;
import com.back.admin.RRHHRepository;
import com.back.auth.event.UsuarioRegistradoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RRHHRegistrationListener {

    private static final Logger logger = LoggerFactory.getLogger(RRHHRegistrationListener.class);

    private final RRHHRepository rrhhRepository;

    @Async
    @EventListener
    public void onUsuarioRegistrado(UsuarioRegistradoEvent event) {
        if ("ROLE_RRHH".equals(event.rol())) {
            logger.info("Procesando post-registro para RRHH: {}", event.email());

            RRHH rrhh = new RRHH();
            rrhh.setId(event.usuarioId());
            rrhh.setUsername(event.username());
            rrhh.setApellido(event.apellido());
            if (event.telefono() != null && !event.telefono().trim().isEmpty()) {
                rrhh.setTelefono(event.telefono());
            }
            rrhhRepository.save(rrhh);

            logger.info("Perfil RRHH creado para: {}", event.email());
        }
    }
}
