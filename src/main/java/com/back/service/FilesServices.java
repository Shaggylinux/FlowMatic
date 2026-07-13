package com.back.service;

import com.back.repository.ArchivosRepository;
import com.back.model.Archivos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class FilesServices {
    @Autowired
    private ArchivosRepository repository;

    private final String rootFolder = "superfolder";

    public void guardarArchivoPorEtapa(MultipartFile file, String emailPropietario, String etapa) throws IOException {
        Path directoryPath = Paths.get(rootFolder, etapa, emailPropietario);
        if (!Files.exists(directoryPath)) {
            Files.createDirectories(directoryPath);
        }

        Path filePath = directoryPath.resolve(file.getOriginalFilename());
        Files.copy(file.getInputStream(), filePath);

        Archivos nuevoArchivo = new Archivos();
        nuevoArchivo.setNombre(file.getOriginalFilename());
        nuevoArchivo.setUbicacion(filePath.toString());
        nuevoArchivo.setPropietario(emailPropietario);
        nuevoArchivo.setEtapa(etapa);
        nuevoArchivo.setEsCarpeta(false);

        repository.save(nuevoArchivo);
    }

    public void compartirArchivo(Long archivoId, String nombreDestinatario) {
        Archivos archivo = repository.findById(archivoId)
            .orElseThrow(() -> new RuntimeException("Archivo no encontrado"));
        
        archivo.setDestinario(nombreDestinatario);
        repository.save(archivo);
        
        System.out.println("Notificación enviada a: " + nombreDestinatario);
    }

    public void crearCarpetaCandidato(String email) {
        try {
            Path dir = Paths.get(rootFolder, "Candidatos", email);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
                Archivos carpeta = new Archivos();
                carpeta.setNombre(email);
                carpeta.setUbicacion(dir.toString().replace("\\", "/"));
                carpeta.setEsCarpeta(true);
                carpeta.setPropietario(email);
                repository.save(carpeta);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Archivos guardarDocumento(MultipartFile file, String email, String tipoDocumento) throws IOException {
        Path dir = Paths.get(rootFolder, "Candidatos", email);
        if (!Files.exists(dir)) Files.createDirectories(dir);
        Path filePath = dir.resolve(file.getOriginalFilename());
        Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        Archivos doc = new Archivos();
        doc.setNombre(file.getOriginalFilename());
        doc.setUbicacion(filePath.toString().replace("\\", "/"));
        doc.setPropietario(email);
        doc.setEsCarpeta(false);
        doc.setTipoDocumento(tipoDocumento != null ? tipoDocumento : "Otro");
        doc.setEtapa("Candidatos");
        return repository.save(doc);
    }
}