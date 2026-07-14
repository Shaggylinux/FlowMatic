package com.back.repository;

import com.back.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByLeidaFalseOrderByFechaDesc();

    long countByLeidaFalse();
}
