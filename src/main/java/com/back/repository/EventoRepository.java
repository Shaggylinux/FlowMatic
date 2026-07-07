package com.back.repository;

import com.back.model.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface EventoRepository extends JpaRepository<Evento, Long> {

    List<Evento> findByFechaBetween(LocalDate start, LocalDate end);

    long countByFecha(LocalDate fecha);

    long countByEstado(String estado);

    List<Evento> findByEstadoAndFechaAfterOrderByFechaAscHoraAsc(String estado, LocalDate fecha);

    @Query("SELECT COUNT(e) FROM Evento e WHERE e.fecha BETWEEN :start AND :end")
    long countByFechaBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COUNT(DISTINCT e.candidatoId) FROM Evento e WHERE e.fecha BETWEEN :start AND :end")
    long countDistinctCandidatoIdByFechaBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    Optional<Evento> findFirstByEstadoAndFechaAfterOrderByFechaAscHoraAsc(String estado, LocalDate fecha);

    boolean existsByCandidatoIdAndFechaAndHora(Long candidatoId, LocalDate fecha, LocalTime hora);

    @Query("SELECT e FROM Evento e WHERE e.fecha BETWEEN :start AND :end " +
           "AND (:candidatoId IS NULL OR e.candidatoId = :candidatoId) " +
           "AND (:estado IS NULL OR e.estado = :estado) " +
           "AND (:rrhhId IS NULL OR e.rrhhId = :rrhhId) " +
           "ORDER BY e.fecha ASC, e.hora ASC")
    List<Evento> findFiltered(@Param("start") LocalDate start,
                              @Param("end") LocalDate end,
                              @Param("candidatoId") Long candidatoId,
                              @Param("estado") String estado,
                              @Param("rrhhId") Long rrhhId);
}
