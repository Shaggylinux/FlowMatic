package com.back.repository;

import com.back.model.Archivos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FilesRepository extends JpaRepository<Archivos, Long> {

    @Query(value = "SELECT * FROM archivos WHERE propietario = :usuario " +
                   "OR destinario = :usuario " +
                   "OR destinario = (SELECT correo FROM usuarios WHERE username = :usuario)", 
           nativeQuery = true)
    List<Archivos> buscarArchivosVisiblesPara(@Param("usuario") String usuario);
}