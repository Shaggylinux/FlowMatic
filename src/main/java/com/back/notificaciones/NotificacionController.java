package com.back.notificaciones;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    @GetMapping
    public ResponseEntity<?> obtenerNoLeidas() {
        return ResponseEntity.ok(new NotificacionListaDTO(
            notificacionService.obtenerNoLeidas().stream()
                .map(n -> new NotificacionDTO(
                    n.getId(), n.getTipo(), n.getMensaje(),
                    n.getCandidatoId(), n.getCandidatoNombre(),
                    n.getFecha(), n.isLeida(), n.getEnlace()
                ))
                .collect(Collectors.toList()),
            notificacionService.contarNoLeidas()
        ));
    }

    @PostMapping("/{id}/leer")
    public ResponseEntity<?> marcarLeida(@PathVariable Long id) {
        notificacionService.marcarLeida(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/leer-todas")
    public ResponseEntity<?> marcarTodasLeidas() {
        notificacionService.marcarTodasLeidas();
        return ResponseEntity.ok(Map.of("success", true));
    }
}
