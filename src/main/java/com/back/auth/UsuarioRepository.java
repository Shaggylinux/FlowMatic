package com.back.auth;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    List<Usuario> findByRol(String rol);
    List<Usuario> findTop10ByOrderByIdDesc();
    long countByRol(String rol);
    long countByRolAndActivo(String rol, boolean activo);
    long countByRolAndBloqueado(String rol, boolean bloqueado);
    long countByRolAndActivoAndBloqueado(String rol, boolean activo, boolean bloqueado);
    long countByActivoTrue();
    long countByActivoFalse();

    Page<Usuario> findAll(Pageable pageable);
    Page<Usuario> findByRol(String rol, Pageable pageable);
    Page<Usuario> findByEmailContainingIgnoreCase(String email, Pageable pageable);
    Page<Usuario> findByRolAndEmailContainingIgnoreCase(String rol, String email, Pageable pageable);

    void deleteByEmail(String email);

    /**
     * Búsqueda multicriterio sobre usuarios RRHH.
     * Combina texto libre (email, nombre, apellido, documento via JOIN con RRHH)
     * con filtro de estado (Activo / Pendiente / Bloqueado).
     */
    @Query("""
        SELECT u FROM Usuario u
        LEFT JOIN RRHH r ON r.id = u.id
        WHERE u.rol = 'ROLE_RRHH'
          AND (
            :buscar IS NULL OR :buscar = '' OR
            LOWER(u.email)      LIKE LOWER(CONCAT('%', :buscar, '%')) OR
            LOWER(r.username)   LIKE LOWER(CONCAT('%', :buscar, '%')) OR
            LOWER(r.apellido)   LIKE LOWER(CONCAT('%', :buscar, '%')) OR
            LOWER(r.documento)  LIKE LOWER(CONCAT('%', :buscar, '%'))
          )
          AND (
            :estado IS NULL OR :estado = '' OR
            (:estado = 'Bloqueado'  AND u.bloqueado = true) OR
            (:estado = 'Activo'     AND u.activo = true  AND u.bloqueado = false) OR
            (:estado = 'Pendiente'  AND u.activo = false AND u.bloqueado = false)
          )
        ORDER BY u.id DESC
        """)
    Page<Usuario> buscarRRHH(@Param("buscar") String buscar,
                              @Param("estado") String estado,
                              Pageable pageable);

    /** Versión sin paginación para exportar todo el resultado filtrado */
    @Query("""
        SELECT u FROM Usuario u
        LEFT JOIN RRHH r ON r.id = u.id
        WHERE u.rol = 'ROLE_RRHH'
          AND (
            :buscar IS NULL OR :buscar = '' OR
            LOWER(u.email)      LIKE LOWER(CONCAT('%', :buscar, '%')) OR
            LOWER(r.username)   LIKE LOWER(CONCAT('%', :buscar, '%')) OR
            LOWER(r.apellido)   LIKE LOWER(CONCAT('%', :buscar, '%')) OR
            LOWER(r.documento)  LIKE LOWER(CONCAT('%', :buscar, '%'))
          )
          AND (
            :estado IS NULL OR :estado = '' OR
            (:estado = 'Bloqueado'  AND u.bloqueado = true) OR
            (:estado = 'Activo'     AND u.activo = true  AND u.bloqueado = false) OR
            (:estado = 'Pendiente'  AND u.activo = false AND u.bloqueado = false)
          )
        ORDER BY u.id DESC
        """)
    List<Usuario> buscarRRHHSinPaginacion(@Param("buscar") String buscar,
                                          @Param("estado") String estado);
}
