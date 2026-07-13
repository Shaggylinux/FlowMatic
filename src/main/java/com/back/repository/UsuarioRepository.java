package com.back.repository;

import com.back.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);
    Usuario findByUsername(String username);
    Optional<Usuario> findByTokenactivacion(String token);
    List<Usuario> findByRol(String rol);

    @Query(value = "SELECT * FROM usuarios WHERE rol = 'ROLE_CANDIDATO' " +
           "AND (cast(:search as text) IS NULL OR LOWER(username) LIKE LOWER(cast(:search as text)) " +
           "OR LOWER(apellido) LIKE LOWER(cast(:search as text)) " +
           "OR LOWER(email) LIKE LOWER(cast(:search as text))) " +
           "AND (cast(:cargo as text) IS NULL OR LOWER(cargo) LIKE LOWER(cast(:cargo as text))) " +
           "AND (cast(:estado as text) IS NULL OR estado = cast(:estado as text)) " +
           "AND (:experienciaMin IS NULL OR experiencia >= :experienciaMin) " +
           "AND (cast(:ciudad as text) IS NULL OR LOWER(ciudad) LIKE LOWER(cast(:ciudad as text))) " +
           "ORDER BY ultima_actualizacion DESC NULLS LAST",
           countQuery = "SELECT count(*) FROM usuarios WHERE rol = 'ROLE_CANDIDATO' " +
           "AND (cast(:search as text) IS NULL OR LOWER(username) LIKE LOWER(cast(:search as text)) " +
           "OR LOWER(apellido) LIKE LOWER(cast(:search as text)) " +
           "OR LOWER(email) LIKE LOWER(cast(:search as text))) " +
           "AND (cast(:cargo as text) IS NULL OR LOWER(cargo) LIKE LOWER(cast(:cargo as text))) " +
           "AND (cast(:estado as text) IS NULL OR estado = cast(:estado as text)) " +
           "AND (:experienciaMin IS NULL OR experiencia >= :experienciaMin) " +
           "AND (cast(:ciudad as text) IS NULL OR LOWER(ciudad) LIKE LOWER(cast(:ciudad as text)))",
           nativeQuery = true)
    Page<Usuario> findCandidatosFiltrados(
            @Param("search") String search,
            @Param("cargo") String cargo,
            @Param("estado") String estado,
            @Param("experienciaMin") Integer experienciaMin,
            @Param("ciudad") String ciudad,
            Pageable pageable);

    long countByRol(String rol);
    long countByActivoTrue();
    long countByActivoFalse();
    List<Usuario> findTop10ByOrderByIdDesc();

    long countByRolAndActivo(String rol, boolean activo);

    long countByRolAndUltimaActualizacionAfter(String rol, LocalDateTime since);

    long countByRolAndEstadoIn(String rol, List<String> estados);

    @Query("SELECT DISTINCT u.ciudad FROM Usuario u WHERE u.rol = 'ROLE_CANDIDATO' AND u.ciudad IS NOT NULL AND u.ciudad <> ''")
    List<String> findDistinctCiudadesByRolCandidato();

    @Query("SELECT DISTINCT u.cargo FROM Usuario u WHERE u.rol = 'ROLE_CANDIDATO' AND u.cargo IS NOT NULL AND u.cargo <> ''")
    List<String> findDistinctCargosByRolCandidato();
}