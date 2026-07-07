package com.back.service;

import com.back.model.Evento;
import com.back.model.Usuario;
import com.back.repository.EventoRepository;
import com.back.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

@Service
public class EventoService {

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Evento crearEvento(Long candidatoId, LocalDate fecha, LocalTime hora,
                              String tipo, String lugar, String observaciones,
                              String estado, Long rrhhId) {

        if (fecha.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha no puede ser anterior a hoy");
        }
        if (fecha.equals(LocalDate.now()) && hora.isBefore(LocalTime.now().withSecond(0).withNano(0))) {
            throw new IllegalArgumentException("La hora seleccionada ya pasó");
        }

        if (hora.isBefore(LocalTime.of(7, 0)) || hora.isAfter(LocalTime.of(19, 0))) {
            throw new IllegalArgumentException("La hora debe estar entre 07:00 y 19:00");
        }

        if (eventoRepository.existsByCandidatoIdAndFechaAndHora(candidatoId, fecha, hora)) {
            throw new IllegalArgumentException("El candidato ya tiene una entrevista en esa fecha y hora");
        }

        if (lugar != null && lugar.length() > 200) {
            throw new IllegalArgumentException("El lugar no puede tener más de 200 caracteres");
        }

        if (observaciones != null && observaciones.length() > 500) {
            throw new IllegalArgumentException("Las observaciones no pueden tener más de 500 caracteres");
        }

        Usuario candidato = usuarioRepository.findById(candidatoId)
                .orElseThrow(() -> new IllegalArgumentException("Candidato no encontrado"));

        Evento evento = new Evento();
        evento.setCandidatoId(candidatoId);
        evento.setCandidatoNombre(candidato.getUsername() + " " + candidato.getApellido());
        evento.setFecha(fecha);
        evento.setHora(hora);
        evento.setTipo(tipo != null ? tipo : "ENTREVISTA_INICIAL");
        evento.setEstado(estado != null ? estado : "PENDIENTE");
        evento.setLugar(lugar);
        evento.setObservaciones(observaciones);
        evento.setRrhhId(rrhhId);

        return eventoRepository.save(evento);
    }

    public List<Evento> obtenerEventosEnRango(LocalDate start, LocalDate end) {
        return eventoRepository.findByFechaBetween(start, end);
    }

    public List<Evento> obtenerEventosFiltrados(LocalDate start, LocalDate end,
                                                 Long candidatoId, String estado, Long rrhhId) {
        return eventoRepository.findFiltered(start, end, candidatoId, estado, rrhhId);
    }

    public Evento buscarPorId(Long id) {
        return eventoRepository.findById(id).orElse(null);
    }

    public Evento actualizarEstado(Long id, String nuevoEstado) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
        evento.setEstado(nuevoEstado);
        return eventoRepository.save(evento);
    }

    public long contarHoy() {
        return eventoRepository.countByFecha(LocalDate.now());
    }

    public long contarPendientes() {
        return eventoRepository.countByEstado("PENDIENTE");
    }

    public long contarConfirmadas() {
        return eventoRepository.countByEstado("CONFIRMADO");
    }

    public long contarReprogramadas() {
        return eventoRepository.countByEstado("REPROGRAMADO");
    }

    public long contarCanceladas() {
        return eventoRepository.countByEstado("CANCELADO");
    }

    public long contarTotalEsteMes() {
        LocalDate now = LocalDate.now();
        LocalDate start = now.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate end = now.with(TemporalAdjusters.lastDayOfMonth());
        return eventoRepository.countByFechaBetween(start, end);
    }

    public long contarCandidatosUnicosEsteMes() {
        LocalDate now = LocalDate.now();
        LocalDate start = now.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate end = now.with(TemporalAdjusters.lastDayOfMonth());
        return eventoRepository.countDistinctCandidatoIdByFechaBetween(start, end);
    }

    public Optional<Evento> obtenerProximaEntrevista() {
        return eventoRepository.findFirstByEstadoAndFechaAfterOrderByFechaAscHoraAsc("PENDIENTE", LocalDate.now().minusDays(1));
    }

    public void eliminarEvento(Long id) {
        eventoRepository.deleteById(id);
    }
}
