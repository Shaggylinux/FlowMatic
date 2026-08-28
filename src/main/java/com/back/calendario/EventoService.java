package com.back.calendario;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
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

    @Transactional
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

    @Transactional(readOnly = true)
    public List<EventoCalendarioDTO> obtenerFeedCalendario(LocalDate start, LocalDate end,
                                                           Long candidatoId, String estado, Long rrhhId) {
        String estadoNormalizado = (estado != null && !estado.isBlank()) ? estado.trim() : null;
        List<Evento> eventos = eventoRepository.findFiltered(start, end, candidatoId, estadoNormalizado, rrhhId);
        List<EventoCalendarioDTO> dtoList = new ArrayList<>(eventos.size());
        for (int i = 0; i < eventos.size(); i++) {
            dtoList.add(EventoCalendarioDTO.from(eventos.get(i)));
        }
        return dtoList;
    }

    @Transactional(readOnly = true)
    public List<Evento> obtenerProximasEntrevistas(int limite) {
        return eventoRepository.findByFechaAfterOrderByFechaAscHoraAsc(LocalDate.now().minusDays(1))
                .stream().limit(limite).toList();
    }

    @Transactional(readOnly = true)
    public List<Evento> obtenerProximasEntrevistasDesdeHoy(int limite) {
        return eventoRepository.findTop5ByFechaGreaterThanEqualOrderByFechaAscHoraAsc(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public long contarFecha(LocalDate fecha) {
        return eventoRepository.countByFecha(fecha);
    }

    @Transactional(readOnly = true)
    public List<Evento> obtenerEventosEnRango(LocalDate start, LocalDate end) {
        return eventoRepository.findByFechaBetweenOrderByFechaAscHoraAsc(start, end);
    }

    @Transactional(readOnly = true)
    public List<Evento> obtenerEventosFiltrados(LocalDate start, LocalDate end,
                                                 Long candidatoId, String estado, Long rrhhId) {
        String estadoNormalizado = (estado != null && !estado.isBlank()) ? estado.trim() : null;
        return eventoRepository.findFiltered(start, end, candidatoId, estadoNormalizado, rrhhId);
    }

    @Transactional(readOnly = true)
    public Evento buscarPorId(Long id) {
        return eventoRepository.findById(id).orElse(null);
    }

    @Transactional
    public Evento actualizarEstado(Long id, String nuevoEstado) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
        evento.setEstado(nuevoEstado);
        return eventoRepository.save(evento);
    }

    @Transactional
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

    @Transactional
    public Evento actualizarObservaciones(Long id, String observaciones) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
        EventoValidator.validateObservaciones(observaciones);
        evento.setObservaciones(observaciones);
        return eventoRepository.save(evento);
    }

    @Transactional(readOnly = true)
    public long contarHoy() {
        return eventoRepository.countByFecha(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public long contarPendientes() {
        return eventoRepository.countByEstado("PENDIENTE");
    }

    @Transactional(readOnly = true)
    public long contarConfirmadas() {
        return eventoRepository.countByEstado("CONFIRMADO");
    }

    @Transactional(readOnly = true)
    public long contarReprogramadas() {
        return eventoRepository.countByEstado("REPROGRAMADO");
    }

    @Transactional(readOnly = true)
    public long contarCanceladas() {
        return eventoRepository.countByEstado("CANCELADO");
    }

    @Transactional(readOnly = true)
    public long contarTotalEsteMes() {
        LocalDate now = LocalDate.now();
        LocalDate start = now.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate end = now.with(TemporalAdjusters.lastDayOfMonth());
        return eventoRepository.countByFechaBetween(start, end);
    }

    @Transactional(readOnly = true)
    public long contarCandidatosUnicosEsteMes() {
        LocalDate now = LocalDate.now();
        LocalDate start = now.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate end = now.with(TemporalAdjusters.lastDayOfMonth());
        return eventoRepository.countDistinctCandidatoIdByFechaBetween(start, end);
    }

    @Transactional(readOnly = true)
    public Optional<Evento> obtenerProximaEntrevista() {
        return eventoRepository.findFirstByEstadoAndFechaAfterOrderByFechaAscHoraAsc("PENDIENTE", LocalDate.now().minusDays(1));
    }

    @Transactional
    public void eliminarEvento(Long id) {
        eventoRepository.deleteById(id);
    }

    @EventListener
    @Transactional
    public void onCandidatoEliminado(CandidatoEliminadoEvent event) {
        eventoRepository.deleteByCandidatoId(event.candidatoId());
    }

    @EventListener
    @Transactional
    public void onRRHHEliminado(RRHHEliminadoEvent event) {
        eventoRepository.deleteByRrhhId(event.rrhhId());
    }

    @Transactional(readOnly = true)
    public boolean existePorCandidatoFechaHora(Long candidatoId, LocalDate fecha, LocalTime hora) {
        return eventoRepository.existsByCandidatoIdAndFechaAndHora(candidatoId, fecha, hora);
    }

    @Transactional
    public Evento guardar(Evento evento) {
        return eventoRepository.save(evento);
    }

    @Transactional(readOnly = true)
    public List<Evento> buscarPorCandidatoIdOrdenado(Long candidatoId) {
        return eventoRepository.findByCandidatoIdOrderByFechaDescHoraDesc(candidatoId);
    }

    @Transactional(readOnly = true)
    public List<Evento> buscarTodos() {
        return eventoRepository.findAll();
    }
}
