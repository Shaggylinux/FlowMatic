package com.back.auth;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    List<Usuario> findByRol(String rol);
    List<Usuario> findTop10ByOrderByIdDesc();
    long countByRol(String rol);
    long countByRolAndActivo(String rol, boolean activo);
    long countByActivoTrue();
    long countByActivoFalse();

    Page<Usuario> findAll(Pageable pageable);
    Page<Usuario> findByRol(String rol, Pageable pageable);
    Page<Usuario> findByEmailContainingIgnoreCase(String email, Pageable pageable);
    Page<Usuario> findByRolAndEmailContainingIgnoreCase(String rol, String email, Pageable pageable);

    void deleteByEmail(String email);
}
