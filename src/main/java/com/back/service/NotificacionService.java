package com.back.service;

import com.back.model.Notificacion;
import com.back.repository.NotificacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificacionService {

    @Autowired
    private NotificacionRepository notificacionRepository;

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

    public long contarNoLeidas() {
        return notificacionRepository.countByLeidaFalse();
    }

    public void marcarLeida(Long id) {
        notificacionRepository.findById(id).ifPresent(n -> {
            n.setLeida(true);
            notificacionRepository.save(n);
        });
    }

    public void marcarTodasLeidas() {
        List<Notificacion> noLeidas = notificacionRepository.findByLeidaFalseOrderByFechaDesc();
        for (Notificacion n : noLeidas) {
            n.setLeida(true);
            notificacionRepository.save(n);
        }
    }
}
