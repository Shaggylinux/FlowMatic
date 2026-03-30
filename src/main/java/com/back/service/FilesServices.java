package com.back.service;

import com.back.repository.FilesRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.back.model.Archivos;

@Service
public class FilesServices {

    @Autowired
    private FilesRepository repository;

    public void compartirArchivo(Long archivoId, String nombreDestinatario) {
        Archivos archivo = repository.findById(archivoId)
            .orElseThrow(() -> new RuntimeException("Archivo no encontrado"));
        
        archivo.setDestinario(nombreDestinatario);
        repository.save(archivo);
        
        System.out.println("Notificación enviada a: " + nombreDestinatario);
    }
}