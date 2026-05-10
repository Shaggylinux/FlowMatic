package com.back.repository;

import com.back.model.Comentarios;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComentariosRepository extends JpaRepository<Comentarios, Long> {
    Comentarios findByNombre(String nombre);
}