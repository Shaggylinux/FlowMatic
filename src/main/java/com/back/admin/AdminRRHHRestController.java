package com.back.admin;

import com.back.admin.dto.UsuarioRRHHDTO;
import com.back.auth.Usuario;
import com.back.auth.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/admin/api/rrhh")
public class AdminRRHHRestController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RRHHRepository rrhhRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioRRHHDTO> getUsuarioRRHH(@PathVariable Long id) {
        Usuario u = usuarioRepository.findById(id).orElse(null);
        if (u == null || !"ROLE_RRHH".equals(u.getRol())) {
            return ResponseEntity.notFound().build();
        }

        UsuarioRRHHDTO dto = new UsuarioRRHHDTO();
        dto.setId(u.getId());
        dto.setEmail(u.getEmail());
        dto.setRol(u.getRol());
        dto.setActivo(u.isActivo());
        dto.setEstado(u.isActivo() ? "Activo" : "Pendiente");

        RRHH rrhh = rrhhRepository.findById(id).orElse(null);
        if (rrhh != null) {
            dto.setNombre(rrhh.getUsername());
            dto.setApellido(rrhh.getApellido());
            dto.setDocumento(rrhh.getDocumento());
            dto.setCargo(rrhh.getCargo());
            dto.setUltimoAcceso(rrhh.getUltimoAcceso());
        }

        return ResponseEntity.ok(dto);
    }



    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updateUsuarioRRHH(@PathVariable Long id, @RequestBody UsuarioRRHHDTO request) {
        try {
            Usuario u = usuarioRepository.findById(id).orElse(null);
            if (u == null) return ResponseEntity.notFound().build();

            u.setEmail(request.getEmail());
            if (request.getClave() != null && !request.getClave().isBlank()) {
                u.setClave(passwordEncoder.encode(request.getClave()));
            }
            usuarioRepository.save(u);

            RRHH rrhh = rrhhRepository.findById(id).orElse(new RRHH());
            rrhh.setId(u.getId());
            rrhh.setUsername(request.getNombre());
            rrhh.setApellido(request.getApellido());
            rrhh.setTelefono(request.getTelefono());
            rrhh.setDocumento(request.getDocumento());
            rrhh.setCargo(request.getCargo());
            rrhhRepository.save(rrhh);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error interno: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> toggleEstado(@PathVariable Long id) {
        Usuario u = usuarioRepository.findById(id).orElse(null);
        if (u == null) return ResponseEntity.notFound().build();

        u.setActivo(!u.isActivo());
        usuarioRepository.save(u);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/toggle-bloqueo")
    public ResponseEntity<?> toggleBloqueo(@PathVariable Long id) {
        Usuario u = usuarioRepository.findById(id).orElse(null);
        if (u == null) return ResponseEntity.notFound().build();
        u.setBloqueado(!u.isBloqueado());
        usuarioRepository.save(u);
        return ResponseEntity.ok(Map.of("bloqueado", u.isBloqueado()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUsuario(@PathVariable Long id) {
        rrhhRepository.deleteById(id);
        usuarioRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
