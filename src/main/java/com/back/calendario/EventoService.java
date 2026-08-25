package com.back.calendario;

import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;
import org.springframework.context.event.EventListener;
import com.back.shared.event.CandidatoEliminadoEvent;
import com.back.shared.event.RRHHEliminadoEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;

    public Evento crearEvento(Long candidatoId, String candidatoNombre, LocalDate fecha, LocalTime hora,
                              String tipo, String lugar, String vacante,
                              String modalidad, String entrevistador,
                              String observaciones, String estado, Long rrhhId) {

        EventoValidator.validate(fecha, hora, lugar, observaciones);

        if (eventoRepository.existsByCandidatoIdAndFechaAndHora(candidatoId, fecha, hora)) {
            throw new IllegalArgumentException("El candidato ya tiene una entrevista en esa fecha y hora");
        }

        Evento evento = new Evento();
        evento.setCandidatoId(candidatoId);
        evento.setCandidatoNombre(candidatoNombre);
        evento.setFecha(fecha);
        evento.setHora(hora);
        evento.setTipo(tipo != null ? tipo : "ENTREVISTA_INICIAL");
        evento.setEstado(estado != null ? estado : "PENDIENTE");
        evento.setLugar(lugar);
        evento.setVacante(vacante);
        evento.setModalidad(modalidad);
        evento.setEntrevistador(entrevistador);
        evento.setObservaciones(observaciones);
        evento.setRrhhId(rrhhId);

        return eventoRepository.save(evento);
    }

    public List<Evento> obtenerProximasEntrevistas(int limite) {
        return eventoRepository.findByFechaAfterOrderByFechaAscHoraAsc(LocalDate.now().minusDays(1))
                .stream().limit(limite).toList();
    }

    public List<Evento> obtenerProximasEntrevistasDesdeHoy(int limite) {
        return eventoRepository.findTop5ByFechaGreaterThanEqualOrderByFechaAscHoraAsc(LocalDate.now());
    }

    public long contarFecha(LocalDate fecha) {
        return eventoRepository.countByFecha(fecha);
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

    public Evento actualizarEvento(Long id, LocalDate fecha, LocalTime hora,
                                    String tipo, String lugar, String vacante,
                                    String modalidad, String entrevistador,
                                    String observaciones) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

        EventoValidator.validate(fecha, hora, lugar, observaciones);

        if (eventoRepository.existsByCandidatoIdAndFechaAndHora(evento.getCandidatoId(), fecha, hora)) {
            Evento existente = eventoRepository
                .findFirstByCandidatoIdAndFechaAndHora(evento.getCandidatoId(), fecha, hora).orElse(null);
            if (existente != null && !existente.getId().equals(id)) {
                throw new IllegalArgumentException("El candidato ya tiene una entrevista en esa fecha y hora");
            }
        }

        evento.setFecha(fecha);
        evento.setHora(hora);
        evento.setTipo(tipo != null ? tipo : "ENTREVISTA_INICIAL");
        evento.setEstado("REPROGRAMADO");
        evento.setLugar(lugar);
        evento.setVacante(vacante);
        evento.setModalidad(modalidad);
        evento.setEntrevistador(entrevistador);
        evento.setObservaciones(observaciones);

        return eventoRepository.save(evento);
    }

    public Evento actualizarObservaciones(Long id, String observaciones) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
        EventoValidator.validateObservaciones(observaciones);
        evento.setObservaciones(observaciones);
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

    @EventListener
    public void onCandidatoEliminado(CandidatoEliminadoEvent event) {
        eventoRepository.deleteByCandidatoId(event.candidatoId());
    }

    @EventListener
    public void onRRHHEliminado(RRHHEliminadoEvent event) {
        eventoRepository.deleteByRrhhId(event.rrhhId());
    }

    public boolean existePorCandidatoFechaHora(Long candidatoId, LocalDate fecha, LocalTime hora) {
        return eventoRepository.existsByCandidatoIdAndFechaAndHora(candidatoId, fecha, hora);
    }

    @org.springframework.transaction.annotation.Transactional
    public Evento guardar(Evento evento) {
        return eventoRepository.save(evento);
    }

    public List<Evento> buscarPorCandidatoIdOrdenado(Long candidatoId) {
        return eventoRepository.findByCandidatoIdOrderByFechaDescHoraDesc(candidatoId);
    }

    public List<Evento> buscarTodos() {
        return eventoRepository.findAll();
    }
}
