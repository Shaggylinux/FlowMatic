package com.back.admin;

import com.back.admin.dto.UsuarioRRHHDTO;
import com.back.auth.Usuario;
import com.back.auth.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@RestController
@RequestMapping("/admin/api/rrhh")
@RequiredArgsConstructor
public class AdminRRHHRestController {

    private final UsuarioRepository usuarioRepository;
    private final RRHHRepository rrhhRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;

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
        dto.setBloqueado(u.isBloqueado());
        dto.setEstado(u.isBloqueado() ? "Bloqueado" : (u.isActivo() ? "Activo" : "Pendiente"));

        RRHH rrhh = rrhhRepository.findById(id).orElse(null);
        if (rrhh != null) {
            dto.setNombre(rrhh.getUsername());
            dto.setApellido(rrhh.getApellido());
            dto.setTelefono(rrhh.getTelefono());
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

            if (request.getNombre() == null || request.getNombre().isBlank()
                    || !request.getNombre().matches("^[A-Za-záéíóúÁÉÍÓÚñÑ\\s]{2,50}$")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El nombre es obligatorio y debe contener solo letras (de 2 a 50 caracteres)"));
            }

            if (request.getApellido() == null || request.getApellido().isBlank()
                    || !request.getApellido().matches("^[A-Za-záéíóúÁÉÍÓÚñÑ\\s]{2,50}$")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El apellido es obligatorio y debe contener solo letras (de 2 a 50 caracteres)"));
            }

            if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(u.getEmail())) {
                boolean duplicado = usuarioRepository.findByEmail(request.getEmail())
                        .map(existente -> !existente.getId().equals(id))
                        .orElse(false);
                if (duplicado) {
                    return ResponseEntity.status(409).body(Map.of("error", "Email duplicado"));
                }
            }

            if (request.getClave() != null && !request.getClave().isBlank()
                    && !com.back.util.ValidadorClave.esClaveSegura(request.getClave())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "La contraseña debe tener mínimo 8 caracteres, mayúsculas, minúsculas, un número y un carácter especial"));
            }

            if (request.getTelefono() != null && !request.getTelefono().isBlank()
                    && (!request.getTelefono().matches("^[0-9]+$") || request.getTelefono().length() != 10)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El teléfono debe contener 10 dígitos numéricos"));
            }

            if (request.getDocumento() != null && !request.getDocumento().isBlank()
                    && (!request.getDocumento().matches("^[0-9]+$") || request.getDocumento().length() > 10)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El documento debe contener solo números y máximo 10 dígitos"));
            }

            if (request.getCargo() != null && !request.getCargo().isBlank()
                    && !request.getCargo().matches("^[A-Za-záéíóúÁÉÍÓÚñÑ\\s]+$")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El cargo debe contener solo letras"));
            }

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
            return ResponseEntity.internalServerError().body(Map.of("error", "Error interno al actualizar el usuario"));
        }
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
    @Transactional
    public ResponseEntity<?> deleteUsuario(@PathVariable Long id, java.security.Principal principal) {
        try {
            Usuario u = usuarioRepository.findById(id).orElse(null);
            if (u == null) {
                return ResponseEntity.notFound().build();
            }
            String email = u.getEmail();
            eventPublisher.publishEvent(new com.back.shared.event.RRHHEliminadoEvent(id));
            rrhhRepository.deleteById(id);
            usuarioRepository.delete(u);

            String actor = principal != null ? principal.getName() : "Administrador";
            auditoriaService.registrar("ELIMINACIÓN", "Usuario RRHH eliminado: " + email, actor, "SEGURIDAD");

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al eliminar el usuario: " + e.getMessage()));
        }
    }
}
