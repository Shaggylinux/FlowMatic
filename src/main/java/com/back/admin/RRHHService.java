package com.back.admin;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RRHHService {

    private final RRHHRepository rrhhRepository;

    public List<RRHH> buscarTodos() {
        return rrhhRepository.findAll();
    }

    public Optional<RRHH> buscarPorId(Long id) {
        return rrhhRepository.findById(id);
    }

    @Transactional
    public RRHH guardar(RRHH rrhh) {
        return rrhhRepository.save(rrhh);
    }

    @Transactional
    public void eliminar(RRHH rrhh) {
        rrhhRepository.delete(rrhh);
    }

    @Transactional
    public void eliminarPorId(Long id) {
        rrhhRepository.deleteById(id);
    }
}
