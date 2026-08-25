package com.back.candidatos.listener;

import com.back.auth.event.UsuarioRegistradoEvent;
import com.back.candidatos.Candidato;
import com.back.candidatos.CandidatoService;
import com.back.drive.FilesServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CandidatoRegistrationListener {

    private static final Logger logger = LoggerFactory.getLogger(CandidatoRegistrationListener.class);

    private final CandidatoService candidatoService;
    private final FilesServices filesServices;
    private final com.back.shared.HistorialService historialService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onUsuarioRegistrado(UsuarioRegistradoEvent event) {
        if ("ROLE_CANDIDATO".equals(event.rol())) {
            if (candidatoService.existePorId(event.usuarioId())) {
                logger.info("Perfil de candidato ya existe, omitiendo: {}", event.email());
                return;
            }
            logger.info("Procesando post-registro para candidato: {}", event.email());

            Candidato candidato = new Candidato();
            candidato.setId(event.usuarioId());
            candidato.setUsername(event.username());
            candidato.setApellido(event.apellido());
            candidato.setEstado("Registrado");
            candidato.setUltimaActualizacion(java.time.LocalDateTime.now());
            candidato.setRrhhEmail(event.rrhhEmail());
            candidatoService.guardar(candidato);

            historialService.registrarCambio(event.usuarioId(), "Nuevo Registro", "Registrado", "Sistema");

            String nombreCompleto = (event.username() + " " + (event.apellido() != null ? event.apellido() : "")).trim();
            filesServices.asegurarCarpetaCandidato("Candidatos/" + nombreCompleto, nombreCompleto, event.email());

            logger.info("Perfil y carpeta creados para: {}", event.email());
        }
    }
}
