package com.back.shared;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistorialRepository extends JpaRepository<Historial, Long> {

    List<Historial> findByCandidatoIdOrderByFechaDesc(Long candidatoId);

    List<Historial> findByCandidatoIdOrderByFechaAsc(Long candidatoId);
}
