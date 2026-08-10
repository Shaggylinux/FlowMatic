package com.back.candidatos.listener;

import com.back.auth.event.UsuarioRegistradoEvent;
import com.back.candidatos.Candidato;
import com.back.candidatos.CandidatoRepository;
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

    private final CandidatoRepository candidatoRepository;
    private final FilesServices filesServices;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onUsuarioRegistrado(UsuarioRegistradoEvent event) {
        if ("ROLE_CANDIDATO".equals(event.rol())) {
            logger.info("Procesando post-registro para candidato: {}", event.email());

            Candidato candidato = new Candidato();
            candidato.setId(event.usuarioId());
            candidato.setUsername(event.username());
            candidato.setApellido(event.apellido());
            candidato.setRrhhEmail(event.rrhhEmail());
            candidatoRepository.save(candidato);

            String nombreCompleto = (event.username() + " " + (event.apellido() != null ? event.apellido() : "")).trim();
            filesServices.asegurarCarpetaCandidato("Candidatos/" + nombreCompleto, nombreCompleto, event.email());

            logger.info("Perfil y carpeta creados para: {}", event.email());
        }
    }
}
