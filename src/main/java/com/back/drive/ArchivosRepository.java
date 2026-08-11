package com.back.drive;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArchivosRepository extends JpaRepository<Archivos, Long> {

    @Query("SELECT a FROM Archivos a WHERE " +
           "LOWER(a.propietario) = LOWER(:id) OR " +
           "LOWER(a.destinario) = LOWER(:id) OR " +
           "(a.candidato IS NOT NULL AND LOWER(a.candidato.email) = LOWER(:id))")
    List<Archivos> buscarArchivosVisiblesPara(@Param("id") String id);

    @Query("SELECT a FROM Archivos a WHERE a.ubicacion LIKE CONCAT(:prefix, '%') AND a.esCarpeta = false")
    List<Archivos> findByUbicacionStartingWith(@Param("prefix") String prefix);

    @Query("SELECT a FROM Archivos a WHERE a.ubicacion LIKE CONCAT(:prefix, '%') AND a.esCarpeta = true")
    List<Archivos> findFoldersByUbicacionStartingWith(@Param("prefix") String prefix);

    @Query("SELECT a FROM Archivos a WHERE " +
           "(a.candidato IS NOT NULL AND a.candidato.id = :candidatoId) OR " +
           "LOWER(a.propietario) = LOWER(:email) OR " +
           "LOWER(a.destinario) = LOWER(:email)")
    List<Archivos> findByCandidatoIdOrEmail(@Param("candidatoId") Long candidatoId, @Param("email") String email);

    @Query("SELECT count(a) FROM Archivos a WHERE a.esCarpeta = true")
    long countFolders();
}