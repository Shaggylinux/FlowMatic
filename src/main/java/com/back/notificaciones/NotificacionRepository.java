package com.back.notificaciones;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByLeidaFalseOrderByFechaDesc();

    List<Notificacion> findByLeidaFalseAndCandidatoIdOrderByFechaDesc(Long candidatoId);

    List<Notificacion> findTop5ByOrderByFechaDesc();

    List<Notificacion> findTop5ByCandidatoIdOrderByFechaDesc(Long candidatoId);

    List<Notificacion> findTop5ByCandidatoIdIsNullOrderByFechaDesc();

    long countByLeidaFalse();
}
