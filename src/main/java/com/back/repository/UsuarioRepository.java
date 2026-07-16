package com.back.repository;

import com.back.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByTokenactivacion(String token);
    List<Usuario> findByRol(String rol);
    List<Usuario> findTop10ByOrderByIdDesc();
    long countByRol(String rol);
    long countByRolAndActivo(String rol, boolean activo);
    long countByActivoTrue();
    long countByActivoFalse();
}
