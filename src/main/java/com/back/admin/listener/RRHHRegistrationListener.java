package com.back.admin.listener;

import com.back.admin.RRHH;
import com.back.admin.RRHHService;
import com.back.auth.event.UsuarioRegistradoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RRHHRegistrationListener {

    private static final Logger logger = LoggerFactory.getLogger(RRHHRegistrationListener.class);

    private final RRHHService rrhhService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onUsuarioRegistrado(UsuarioRegistradoEvent event) {
        if ("ROLE_RRHH".equals(event.rol())) {
            if (rrhhService.existePorId(event.usuarioId())) {
                logger.info("Perfil RRHH ya existe, omitiendo: {}", event.email());
                return;
            }
            logger.info("Procesando post-registro para RRHH: {}", event.email());

            RRHH rrhh = new RRHH();
            rrhh.setId(event.usuarioId());
            rrhh.setUsername(event.username());
            rrhh.setApellido(event.apellido());
            if (event.telefono() != null && !event.telefono().trim().isEmpty()) {
                rrhh.setTelefono(event.telefono());
            }
            if (event.documento() != null && !event.documento().trim().isEmpty()) {
                rrhh.setDocumento(event.documento());
            }
            if (event.cargo() != null && !event.cargo().trim().isEmpty()) {
                rrhh.setCargo(event.cargo());
            }
            rrhhService.guardar(rrhh);

            logger.info("Perfil RRHH creado para: {}", event.email());
        }
    }
}
