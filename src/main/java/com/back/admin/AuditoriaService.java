package com.back.admin;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AuditoriaService {

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    public void registrar(String accion, String descripcion, String realizadoPor, String tipo) {
        Auditoria a = new Auditoria();
        a.setAccion(accion);
        a.setDescripcion(descripcion);
        a.setRealizadoPor(realizadoPor);
        a.setTipo(tipo);
        a.setFecha(LocalDateTime.now());
        auditoriaRepository.save(a);
    }

    public Page<Auditoria> obtenerPaginado(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return auditoriaRepository.findAllByOrderByFechaDesc(pageable);
    }

    public List<Auditoria> obtenerRecientes(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return auditoriaRepository.findAllByOrderByFechaDesc(pageable).getContent();
    }

    public long contar() {
        return auditoriaRepository.count();
    }

    public Page<Auditoria> obtenerPorTipo(String tipo, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return auditoriaRepository.findByTipoOrderByFechaDesc(tipo, pageable);
    }

    public long contarPorTipo(String tipo) {
        return auditoriaRepository.countByTipo(tipo);
    }
}
