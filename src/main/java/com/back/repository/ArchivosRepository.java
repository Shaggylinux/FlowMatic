package com.back.repository;

import com.back.model.Archivos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArchivosRepository extends JpaRepository<Archivos, Long> {

@Query("SELECT a FROM Archivos a WHERE " +
       "LOWER(a.propietario) = LOWER(:id) OR LOWER(a.destinario) = LOWER(:id)")
List<Archivos> buscarArchivosVisiblesPara(@Param("id") String id);

@Query("SELECT a FROM Archivos a WHERE a.ubicacion LIKE CONCAT(:prefix, '%') AND a.esCarpeta = false")
List<Archivos> findByUbicacionStartingWith(@Param("prefix") String prefix);

@Query("SELECT a FROM Archivos a WHERE a.ubicacion LIKE CONCAT(:prefix, '%') AND a.esCarpeta = true")
List<Archivos> findFoldersByUbicacionStartingWith(@Param("prefix") String prefix);
}