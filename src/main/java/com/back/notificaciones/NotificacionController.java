package com.back.notificaciones;


import com.back.auth.Usuario;
import com.back.auth.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;
    private final UsuarioRepository usuarioRepository;

    @GetMapping
    public ResponseEntity<?> obtenerNoLeidas(Principal principal) {
        List<Notificacion> noLeidas;
        long totalNoLeidas;
        String email = principal != null ? principal.getName() : null;
        Usuario user = email != null ? usuarioRepository.findByEmail(email).orElse(null) : null;
        boolean esCandidato = user != null && "ROLE_CANDIDATO".equals(user.getRol());
        if (esCandidato) {
            noLeidas = notificacionService.obtenerNoLeidasPorCandidato(user.getId());
            totalNoLeidas = noLeidas.size();
        } else {
            noLeidas = notificacionService.obtenerNoLeidas();
            totalNoLeidas = notificacionService.contarNoLeidas();
        }
        return ResponseEntity.ok(new NotificacionListaDTO(
            noLeidas.stream()
                .map(n -> new NotificacionDTO(
                    n.getId(), n.getTipo(), n.getMensaje(),
                    n.getCandidatoId(), n.getCandidatoNombre(),
                    n.getFecha(), n.isLeida(), n.getEnlace()
                ))
                .collect(Collectors.toList()),
            totalNoLeidas
        ));
    }

    @PostMapping("/{id}/leer")
    public ResponseEntity<?> marcarLeida(@PathVariable Long id, Principal principal) {
        Usuario user = usuarioLogueado(principal);
        boolean esCandidato = user != null && "ROLE_CANDIDATO".equals(user.getRol());
        notificacionService.marcarLeida(id, esCandidato, user != null ? user.getId() : null);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/leer-todas")
    public ResponseEntity<?> marcarTodasLeidas(Principal principal) {
        Usuario user = usuarioLogueado(principal);
        boolean esCandidato = user != null && "ROLE_CANDIDATO".equals(user.getRol());
        notificacionService.marcarTodasLeidas(esCandidato, user != null ? user.getId() : null);
        return ResponseEntity.ok(Map.of("success", true));
    }

    private Usuario usuarioLogueado(Principal principal) {
        if (principal == null) {
            return null;
        }
        return usuarioRepository.findByEmail(principal.getName()).orElse(null);
    }
}
