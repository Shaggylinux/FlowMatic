package com.back.drive;

import com.back.util.Sanitizer;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FilesServices {
    private final ArchivosRepository repository;

    private final String rootFolder = "superfolder";

    @PostConstruct
    public void init() {
        try {
            Path rutaRaiz = Paths.get(rootFolder);
            if (!Files.exists(rutaRaiz)) {
                Files.createDirectories(rutaRaiz);
            }
        } catch (IOException e) {
            System.err.println("Error: No se pudo crear la carpeta ra\u00edz: " + e.getMessage());
        }
    }

    public void guardarArchivoPorEtapa(MultipartFile file, String emailPropietario, String etapa) throws IOException {
        String filename = file.getOriginalFilename();
        if (!Sanitizer.isValidFileName(filename)) {
            throw new IOException("Nombre de archivo inválido: " + filename);
        }
        Path directoryPath = Paths.get(rootFolder, etapa, emailPropietario);
        if (!Files.exists(directoryPath)) {
            Files.createDirectories(directoryPath);
        }

        Path filePath = directoryPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath);

        Archivos nuevoArchivo = new Archivos();
        nuevoArchivo.setNombre(filename);
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


    public Archivos guardarDocumento(MultipartFile file, String email, String tipoDocumento) throws IOException {
        String filename = file.getOriginalFilename();
        if (!Sanitizer.isValidFileName(filename)) {
            throw new IOException("Nombre de archivo inválido: " + filename);
        }
        Path dir = Paths.get(rootFolder, "Candidatos", email);
        if (!Files.exists(dir)) Files.createDirectories(dir);
        Path filePath = dir.resolve(filename);
        Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        Archivos doc = new Archivos();
        doc.setNombre(filename);
        doc.setUbicacion(filePath.toString().replace("\\", "/"));
        doc.setPropietario(email);
        doc.setEsCarpeta(false);
        doc.setTipoDocumento(tipoDocumento != null ? tipoDocumento : "Otro");
        doc.setEtapa("Candidatos");
        return repository.save(doc);
    }

    public void crearCarpetaDrive(String nombre, String folder, String email) throws IOException {
        String rutaCarpeta = rootFolder + "/" + (folder.isEmpty() ? "" : folder + "/") + nombre + "/";
        Files.createDirectories(Paths.get(rutaCarpeta));
        Archivos carpeta = new Archivos();
        carpeta.setNombre(nombre);
        carpeta.setUbicacion(rutaCarpeta);
        carpeta.setEsCarpeta(true);
        carpeta.setPropietario(email);
        repository.save(carpeta);
    }

    public void asegurarCarpetaCandidato(String rutaCarpeta, String nombreCarpeta, String emailPropietario) {
        try {
            Path dir = Paths.get(rootFolder, rutaCarpeta);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
                // Comprobar si ya existe el registro en DB
                if (repository.findFoldersByUbicacionStartingWith(dir.toString().replace("\\", "/")).isEmpty()) {
                    Archivos carpeta = new Archivos();
                    carpeta.setNombre(nombreCarpeta);
                    carpeta.setUbicacion(dir.toString().replace("\\", "/"));
                    carpeta.setEsCarpeta(true);
                    carpeta.setPropietario(emailPropietario);
                    repository.save(carpeta);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void subirArchivoDrive(MultipartFile archivo, String folder, String email, String filename, com.back.auth.Usuario candidato) throws IOException {
        String rutaDestino = rootFolder + "/" + (folder.isEmpty() ? "" : folder + "/") + filename;
        rutaDestino = rutaDestino.replace("//", "/");
        Path rutaCompleta = Paths.get(rutaDestino);
        Files.createDirectories(rutaCompleta.getParent());
        Files.copy(archivo.getInputStream(), rutaCompleta, StandardCopyOption.REPLACE_EXISTING);

        Archivos doc = new Archivos();
        doc.setNombre(filename);
        doc.setUbicacion(rutaDestino);
        doc.setPropietario(email);
        doc.setCandidato(candidato);
        repository.save(doc);
    }

    public void eliminarArchivo(Archivos archivo) {
        try {
            Files.deleteIfExists(Paths.get(archivo.getUbicacion()));
        } catch (IOException ignored) {}
        repository.delete(archivo);
    }

    public Path obtenerRutaArchivo(Archivos archivo) {
        return Paths.get(archivo.getUbicacion()).normalize();
    }
}