package com.back.notificaciones;


import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;

    public Notificacion crear(String tipo, String mensaje, Long candidatoId, String candidatoNombre, String enlace) {
        Notificacion n = new Notificacion();
        n.setTipo(tipo);
        n.setMensaje(mensaje);
        n.setCandidatoId(candidatoId);
        n.setCandidatoNombre(candidatoNombre);
        n.setFecha(LocalDateTime.now());
        n.setLeida(false);
        n.setEnlace(enlace);
        return notificacionRepository.save(n);
    }

    public List<Notificacion> obtenerNoLeidas() {
        return notificacionRepository.findByLeidaFalseOrderByFechaDesc();
    }

    public List<Notificacion> obtenerNoLeidasPorCandidato(Long candidatoId) {
        return notificacionRepository.findByLeidaFalseAndCandidatoIdOrderByFechaDesc(candidatoId);
    }

    public List<Notificacion> obtenerActividadReciente() {
        return notificacionRepository.findTop5ByOrderByFechaDesc();
    }

    public List<Notificacion> obtenerActividadReciente(Long candidatoId) {
        if (candidatoId == null) {
            return notificacionRepository.findTop5ByCandidatoIdIsNullOrderByFechaDesc();
        }
        return notificacionRepository.findTop5ByCandidatoIdOrderByFechaDesc(candidatoId);
    }

    public long contarNoLeidas() {
        return notificacionRepository.countByLeidaFalse();
    }

    public long contarNoLeidasPorCandidato(Long candidatoId) {
        return notificacionRepository.countByLeidaFalseAndCandidatoId(candidatoId);
    }

    public long contarNoLeidasGlobales() {
        return notificacionRepository.countByLeidaFalseAndCandidatoIdIsNull();
    }

    public void marcarLeida(Long id, boolean esCandidato, Long candidatoId) {
        notificacionRepository.findById(id).ifPresent(n -> {
            if (esCandidato) {
                if (n.getCandidatoId() == null || !n.getCandidatoId().equals(candidatoId)) {
                    return;
                }
            } else if (n.getCandidatoId() != null) {
                return;
            }
            n.setLeida(true);
            notificacionRepository.save(n);
        });
    }

    public void marcarTodasLeidas(boolean esCandidato, Long candidatoId) {
        List<Notificacion> noLeidas = esCandidato
            ? notificacionRepository.findByLeidaFalseAndCandidatoIdOrderByFechaDesc(candidatoId)
            : notificacionRepository.findByLeidaFalseAndCandidatoIdIsNullOrderByFechaDesc();
        for (Notificacion n : noLeidas) {
            n.setLeida(true);
            notificacionRepository.save(n);
        }
    }
}
