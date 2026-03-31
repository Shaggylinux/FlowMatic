package com.back.repository;

import com.back.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreo(String correo);
    Optional<Usuario> findByUsername(String username);
    Optional<Usuario> findByTokenActivacion(String token);
}
