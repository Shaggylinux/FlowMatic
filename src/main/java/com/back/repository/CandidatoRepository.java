package com.back.repository;

import com.back.model.Candidato;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CandidatoRepository extends JpaRepository<Candidato, Long> {

    @Query(value = "SELECT * FROM candidatos " +
           "WHERE (cast(:search as text) IS NULL OR LOWER(username) LIKE LOWER(cast(:search as text)) " +
           "OR LOWER(apellido) LIKE LOWER(cast(:search as text))) " +
           "AND (cast(:cargo as text) IS NULL OR LOWER(cargo) LIKE LOWER(cast(:cargo as text))) " +
           "AND (cast(:estado as text) IS NULL OR estado = cast(:estado as text)) " +
           "AND (:experienciaMin IS NULL OR experiencia >= :experienciaMin) " +
           "AND (cast(:ciudad as text) IS NULL OR LOWER(ciudad) LIKE LOWER(cast(:ciudad as text))) " +
           "ORDER BY ultima_actualizacion DESC NULLS LAST",
           countQuery = "SELECT count(*) FROM candidatos " +
           "WHERE (cast(:search as text) IS NULL OR LOWER(username) LIKE LOWER(cast(:search as text)) " +
           "OR LOWER(apellido) LIKE LOWER(cast(:search as text))) " +
           "AND (cast(:cargo as text) IS NULL OR LOWER(cargo) LIKE LOWER(cast(:cargo as text))) " +
           "AND (cast(:estado as text) IS NULL OR estado = cast(:estado as text)) " +
           "AND (:experienciaMin IS NULL OR experiencia >= :experienciaMin) " +
           "AND (cast(:ciudad as text) IS NULL OR LOWER(ciudad) LIKE LOWER(cast(:ciudad as text)))",
           nativeQuery = true)
    Page<Candidato> findFiltrados(
            @Param("search") String search,
            @Param("cargo") String cargo,
            @Param("estado") String estado,
            @Param("experienciaMin") Integer experienciaMin,
            @Param("ciudad") String ciudad,
            Pageable pageable);

    @Query(value = "SELECT * FROM candidatos " +
           "WHERE (cast(:search as text) IS NULL OR LOWER(username) LIKE LOWER(cast(:search as text)) " +
           "OR LOWER(apellido) LIKE LOWER(cast(:search as text))) " +
           "AND (cast(:estado as text) IS NULL OR estado = cast(:estado as text)) " +
           "ORDER BY ultima_actualizacion DESC NULLS LAST",
           nativeQuery = true)
    List<Candidato> findFiltradosSinPaginar(@Param("search") String search,
                                             @Param("estado") String estado);

    long countByEstadoIn(List<String> estados);

    long countByUltimaActualizacionAfter(LocalDateTime since);

    @Query("SELECT DISTINCT c.ciudad FROM Candidato c WHERE c.ciudad IS NOT NULL AND c.ciudad <> ''")
    List<String> findDistinctCiudades();

    @Query("SELECT DISTINCT c.cargo FROM Candidato c WHERE c.cargo IS NOT NULL AND c.cargo <> ''")
    List<String> findDistinctCargos();
}
