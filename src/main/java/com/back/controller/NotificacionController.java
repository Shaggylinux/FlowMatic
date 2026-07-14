package com.back.controller;

import com.back.service.NotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/notificaciones")
public class NotificacionController {

    @Autowired
    private NotificacionService notificacionService;

    @GetMapping
    public ResponseEntity<?> obtenerNoLeidas() {
        return ResponseEntity.ok(Map.of(
            "notificaciones", notificacionService.obtenerNoLeidas(),
            "total", notificacionService.contarNoLeidas()
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
