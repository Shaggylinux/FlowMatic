package com.back.candidatos.listener;

import com.back.auth.event.UsuarioRegistradoEvent;
import com.back.candidatos.Candidato;
import com.back.candidatos.CandidatoRepository;
import com.back.drive.FilesServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CandidatoRegistrationListener {

    private static final Logger logger = LoggerFactory.getLogger(CandidatoRegistrationListener.class);

    private final CandidatoRepository candidatoRepository;
    private final FilesServices filesServices;

    @Async
    @EventListener
    public void onUsuarioRegistrado(UsuarioRegistradoEvent event) {
        if ("ROLE_CANDIDATO".equals(event.getRol())) {
            logger.info("Procesando post-registro para candidato: {}", event.getEmail());

            Candidato candidato = new Candidato();
            candidato.setId(event.getUsuarioId());
            candidato.setUsername(event.getUsername());
            candidato.setApellido(event.getApellido());
            candidatoRepository.save(candidato);

            filesServices.crearCarpetaCandidato(event.getEmail());
            logger.info("Perfil y carpeta creados para: {}", event.getEmail());
        }
    }
}
