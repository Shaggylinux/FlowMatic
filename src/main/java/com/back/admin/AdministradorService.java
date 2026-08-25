package com.back.admin;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdministradorService {

    private final AdministradorRepository administradorRepository;

    public List<Administrador> buscarTodos() {
        return administradorRepository.findAll();
    }

    public Optional<Administrador> buscarPorId(Long id) {
        return administradorRepository.findById(id);
    }

    @Transactional
    public Administrador guardar(Administrador admin) {
        return administradorRepository.save(admin);
    }

    @Transactional
    public void eliminar(Administrador admin) {
        administradorRepository.delete(admin);
    }

    @Transactional
    public void eliminarPorId(Long id) {
        administradorRepository.deleteById(id);
    }
}
