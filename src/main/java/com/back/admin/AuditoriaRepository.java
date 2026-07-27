package com.back.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    Page<Auditoria> findAllByOrderByFechaDesc(Pageable pageable);

    Page<Auditoria> findByTipoOrderByFechaDesc(String tipo, Pageable pageable);

    long countByTipo(String tipo);
}
