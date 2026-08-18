package com.back.shared;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HistorialService {

    private final HistorialRepository historialRepository;

    @Transactional
    public Historial registrarCambio(Long candidatoId, String estadoAnterior, String estadoNuevo, String responsable) {
        if (candidatoId == null || estadoNuevo == null || estadoNuevo.isBlank()) {
            return null;
        }

        Historial registro = Historial.builder()
                .candidatoId(candidatoId)
                .fecha(LocalDateTime.now())
                .estadoAnterior(estadoAnterior != null && !estadoAnterior.isBlank() ? estadoAnterior : "Registrado")
                .estadoNuevo(estadoNuevo)
                .responsable(responsable != null && !responsable.isBlank() ? responsable : "Sistema")
                .build();

        return historialRepository.save(registro);
    }

    @Transactional(readOnly = true)
    public List<Historial> obtenerHistorialPorCandidato(Long candidatoId) {
        if (candidatoId == null) {
            return Collections.emptyList();
        }
        return historialRepository.findByCandidatoIdOrderByFechaDesc(candidatoId);
    }
}
