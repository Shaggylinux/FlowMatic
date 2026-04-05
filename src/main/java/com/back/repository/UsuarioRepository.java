package com.back.repository;

import com.back.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    Usuario findByUsername(String username);
    Optional<Usuario> findByTokenactivacion(String token);
    List<Usuario> findByRol(String rol);
}