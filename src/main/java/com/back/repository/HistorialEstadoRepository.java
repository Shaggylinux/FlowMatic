package com.back.repository;
import com.back.model.HistorialEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface HistorialEstadoRepository extends JpaRepository<HistorialEstado, Long> {
    Optional<HistorialEstado> findById(Long id);
    HistorialEstado findByEstadoAnterior(String estadoAnterior);
    HistorialEstado findByEstadoNuevo(String estadoNuevo);
    HistorialEstado findByFecha(String fecha);
    HistorialEstado findByResponsable(String responsable);
}