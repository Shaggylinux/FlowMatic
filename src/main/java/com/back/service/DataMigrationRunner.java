package com.back.service;

import com.back.model.Candidato;
import com.back.model.RRHH;
import com.back.model.Administrador;
import com.back.repository.CandidatoRepository;
import com.back.repository.RRHHRepository;
import com.back.repository.AdministradorRepository;
import com.back.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class DataMigrationRunner implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CandidatoRepository candidatoRepository;

    @Autowired
    private RRHHRepository rrhhRepository;

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public void run(String... args) {
        try {
            migrateCandidatos();
            migrateRRHH();
            migrateAdministradores();
        } catch (Exception e) {
            System.out.println("[Migration] No se pudo migrar (probablemente ya migrado): " + e.getMessage());
        }
    }

    private void migrateCandidatos() {
        if (candidatoRepository.count() > 0) return;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT id, username, apellido, telefono, estado, cargo, ciudad, " +
            "tecnologias, idiomas, experiencia, disponibilidad, proceso_actual, " +
            "foto_url, ultima_actualizacion FROM usuarios WHERE rol = 'ROLE_CANDIDATO'"
        );
        if (rows.isEmpty()) return;

        for (Map<String, Object> row : rows) {
            Candidato c = new Candidato();
            c.setId(toLong(row.get("id")));
            c.setUsername((String) row.get("username"));
            c.setApellido((String) row.get("apellido"));
            c.setTelefono((String) row.get("telefono"));
            c.setEstado((String) row.get("estado"));
            c.setCargo((String) row.get("cargo"));
            c.setCiudad((String) row.get("ciudad"));
            c.setTecnologias((String) row.get("tecnologias"));
            c.setIdiomas((String) row.get("idiomas"));
            c.setExperiencia(row.get("experiencia") != null ? toInt(row.get("experiencia")) : null);
            c.setDisponibilidad((String) row.get("disponibilidad"));
            c.setProcesoActual((String) row.get("proceso_actual"));
            c.setFotoUrl((String) row.get("foto_url"));
            c.setUltimaActualizacion(row.get("ultima_actualizacion") != null
                ? ((java.sql.Timestamp) row.get("ultima_actualizacion")).toLocalDateTime()
                : LocalDateTime.now());
            candidatoRepository.save(c);
        }
        System.out.println("[Migration] Migrados " + rows.size() + " candidatos");
    }

    private void migrateRRHH() {
        if (rrhhRepository.count() > 0) return;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT id, username, apellido, telefono, foto_url FROM usuarios WHERE rol = 'ROLE_RRHH'"
        );
        if (rows.isEmpty()) return;

        for (Map<String, Object> row : rows) {
            RRHH r = new RRHH();
            r.setId(toLong(row.get("id")));
            r.setUsername((String) row.get("username"));
            r.setApellido((String) row.get("apellido"));
            r.setTelefono((String) row.get("telefono"));
            r.setFotoUrl((String) row.get("foto_url"));
            rrhhRepository.save(r);
        }
        System.out.println("[Migration] Migrados " + rows.size() + " RRHH");
    }

    private void migrateAdministradores() {
        if (administradorRepository.count() > 0) return;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT id, username, apellido FROM usuarios WHERE rol = 'ROLE_ADMINISTRADOR'"
        );
        if (rows.isEmpty()) return;

        for (Map<String, Object> row : rows) {
            Administrador a = new Administrador();
            a.setId(toLong(row.get("id")));
            a.setUsername((String) row.get("username"));
            a.setApellido((String) row.get("apellido"));
            administradorRepository.save(a);
        }
        System.out.println("[Migration] Migrados " + rows.size() + " administradores");
    }

    private Long toLong(Object val) {
        if (val instanceof Number) return ((Number) val).longValue();
        return null;
    }

    private Integer toInt(Object val) {
        if (val instanceof Number) return ((Number) val).intValue();
        return null;
    }
}
