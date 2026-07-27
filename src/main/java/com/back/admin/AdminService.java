package com.back.admin;

import com.back.admin.dto.ActividadRecienteDTO;
import com.back.admin.dto.UsuarioResumenDTO;
import com.back.auth.Usuario;
import com.back.candidatos.CandidatoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private CandidatoRepository candidatoRepository;

    @Autowired
    private RRHHRepository rrhhRepository;

    public List<ActividadRecienteDTO> buildActividadReciente(List<Usuario> usuarios) {
        List<ActividadRecienteDTO> actividades = new ArrayList<>();
        String[] colores = { "#0D9488", "#0EA5E9", "#8B5CF6", "#F59E0B", "#EF4444" };
        int idx = 0;
        for (Usuario u : usuarios) {
            String nombre = obtenerNombreUsuario(u.getId(), u.getRol());
            String iniciales = obtenerIniciales(u.getId(), u.getRol());
            actividades.add(new ActividadRecienteDTO(
                "Nuevo usuario registrado",
                nombre,
                "Recién registrado",
                u.getRol().replace("ROLE_", ""),
                iniciales,
                colores[idx % colores.length]
            ));
            idx++;
        }
        return actividades;
    }

    public List<UsuarioResumenDTO> buildUltimosUsuariosData(List<Usuario> usuarios) {
        return usuarios.stream().map(this::mapToUsuarioResumen).collect(Collectors.toList());
    }

    public UsuarioResumenDTO mapToUsuarioResumen(Usuario u) {
        String nombre = obtenerNombreUsuario(u.getId(), u.getRol());
        String[] parts = nombre.split(" ", 2);
        return new UsuarioResumenDTO(
            u.getId(),
            parts.length > 0 ? parts[0] : "",
            parts.length > 1 ? parts[1] : "",
            u.getEmail(),
            u.getRol(),
            u.isActivo(),
            "Recién"
        );
    }

    public String obtenerNombreUsuario(Long userId, String rol) {
        if (rol == null) return "Usuario";
        return switch (rol) {
            case "ROLE_CANDIDATO" -> candidatoRepository.findById(userId)
                .map(c -> c.getUsername() + " " + (c.getApellido() != null ? c.getApellido() : "")).orElse("Candidato");
            case "ROLE_RRHH" -> rrhhRepository.findById(userId)
                .map(r -> r.getUsername() + " " + (r.getApellido() != null ? r.getApellido() : "")).orElse("RRHH");
            case "ROLE_ADMINISTRADOR" -> "Administrador";
            default -> "Usuario";
        };
    }

    public String obtenerIniciales(Long userId, String rol) {
        if (rol == null) return "US";
        return switch (rol) {
            case "ROLE_CANDIDATO" -> candidatoRepository.findById(userId)
                .map(c -> {
                    String u = c.getUsername() != null && !c.getUsername().isEmpty() ? c.getUsername().substring(0, 1) : "C";
                    String a = c.getApellido() != null && !c.getApellido().isEmpty() ? c.getApellido().substring(0, 1) : "A";
                    return (u + a).toUpperCase();
                })
                .orElse("CA");
            case "ROLE_RRHH" -> rrhhRepository.findById(userId)
                .map(r -> {
                    String u = r.getUsername() != null && !r.getUsername().isEmpty() ? r.getUsername().substring(0, 1) : "R";
                    String a = r.getApellido() != null && !r.getApellido().isEmpty() ? r.getApellido().substring(0, 1) : "H";
                    return (u + a).toUpperCase();
                })
                .orElse("RH");
            default -> "US";
        };
    }
}
