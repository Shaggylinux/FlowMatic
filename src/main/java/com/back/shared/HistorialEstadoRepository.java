package com.back.shared;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.time.LocalDateTime;

public interface HistorialEstadoRepository extends JpaRepository<HistorialEstado, Long> {
    Optional<HistorialEstado> findById(Long id);
    HistorialEstado findByEstadoAnterior(String estadoAnterior);
    HistorialEstado findByEstadoNuevo(String estadoNuevo);
    HistorialEstado findByFecha(LocalDateTime fecha);
    HistorialEstado findByResponsable(String responsable);
}