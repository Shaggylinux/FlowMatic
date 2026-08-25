package com.back.drive;

import com.back.util.Sanitizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.ZoneId;

import java.util.List;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FilesServices {
    private static final Logger logger = LoggerFactory.getLogger(FilesServices.class);

    private final ArchivosRepository repository;

    private final String rootFolder = "superfolder";

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");

    @PostConstruct
    public void init() {
        try {
            Path rutaRaiz = Paths.get(rootFolder);
            if (!Files.exists(rutaRaiz)) {
                Files.createDirectories(rutaRaiz);
            }
        } catch (IOException e) {
            logger.error("No se pudo crear la carpeta raíz: {}", e.getMessage(), e);
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
        nuevoArchivo.setEstadoDocumento("No aplica");
        nuevoArchivo.setFechaSubida(LocalDateTime.now(ZONA));

        repository.save(nuevoArchivo);
    }

    public void compartirArchivo(Long archivoId, String nombreDestinatario) {
        Archivos archivo = repository.findById(archivoId)
            .orElseThrow(() -> new RuntimeException("Archivo no encontrado"));
        
        if (nombreDestinatario != null && !nombreDestinatario.isBlank()) {
            archivo.setDestinario(nombreDestinatario.trim());
        } else {
            archivo.setDestinario(null);
        }
        repository.save(archivo);
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
        doc.setFechaSubida(LocalDateTime.now(ZONA));
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
            logger.error("Error al asegurar carpeta de candidato: {}", e.getMessage(), e);
        }
    }

    public Archivos subirArchivoDrive(MultipartFile archivo, String folder, String email, String filename, com.back.auth.Usuario candidato) throws IOException {
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
        doc.setFechaSubida(LocalDateTime.now(ZONA));
        return repository.save(doc);
    }

    public void eliminarArchivo(Archivos archivo) {
        try {
            Files.deleteIfExists(Paths.get(archivo.getUbicacion()));
        } catch (IOException e) {
            logger.warn("No se pudo eliminar el archivo físico en disco {}: {}", archivo.getUbicacion(), e.getMessage());
        }
        repository.delete(archivo);
    }

    public Path obtenerRutaArchivo(Archivos archivo) {
        return Paths.get(archivo.getUbicacion()).normalize();
    }

    public void eliminarCarpetaRecursiva(String folderPath) throws IOException {
        String searchPrefix = folderPath;
        if (!searchPrefix.endsWith("/")) {
            searchPrefix += "/";
        }
        
        List<Archivos> subFiles = repository.findByUbicacionStartingWith(searchPrefix);
        List<Archivos> subFolders = repository.findFoldersByUbicacionStartingWith(searchPrefix);
        
        repository.deleteAll(subFiles);
        repository.deleteAll(subFolders);
        
        // Find and delete the folder itself (its path might not end with '/')
        List<Archivos> exactFolder = repository.findFoldersByUbicacionStartingWith(folderPath);
        for(Archivos a : exactFolder) {
            if(a.getUbicacion().equals(folderPath) || a.getUbicacion().equals(folderPath + "/")) {
                repository.delete(a);
            }
        }
        
        org.springframework.util.FileSystemUtils.deleteRecursively(Paths.get(folderPath));
    }

    public void renombrarCarpeta(String oldPath, String newName) throws IOException {
        String normalizedOldPath = oldPath.replace("\\", "/").replaceAll("^/+|/+$", "");
        
        // Find the folder object
        List<Archivos> exactFolder = repository.findFoldersByUbicacionStartingWith(normalizedOldPath);
        Archivos folderToRename = null;
        for(Archivos a : exactFolder) {
            String u = a.getUbicacion().replace("\\", "/").replaceAll("^/+|/+$", "");
            if(u.equals(normalizedOldPath)) {
                folderToRename = a;
                break;
            }
        }
        
        if (folderToRename == null) return;
        
        Path oldPhysicalPath = Paths.get(normalizedOldPath);
        Path newPhysicalPath = oldPhysicalPath.resolveSibling(newName);
        
        String newPathStr = newPhysicalPath.toString().replace("\\", "/");
        
        // Rename physically
        if (Files.exists(oldPhysicalPath)) {
            Files.move(oldPhysicalPath, newPhysicalPath, StandardCopyOption.REPLACE_EXISTING);
        }
        
        // Update the folder itself
        folderToRename.setNombre(newName);
        folderToRename.setUbicacion(newPathStr + "/");
        repository.save(folderToRename);
        
        // Update all contents
        String oldPrefix = normalizedOldPath + "/";
        String newPrefix = newPathStr + "/";
        
        List<Archivos> subFiles = repository.findByUbicacionStartingWith(oldPrefix);
        for (Archivos f : subFiles) {
            String current = f.getUbicacion().replace("\\", "/");
            f.setUbicacion(current.replaceFirst("^" + java.util.regex.Pattern.quote(oldPrefix), java.util.regex.Matcher.quoteReplacement(newPrefix)));
        }
        repository.saveAll(subFiles);
        
        List<Archivos> subFolders = repository.findFoldersByUbicacionStartingWith(oldPrefix);
        for (Archivos f : subFolders) {
            String current = f.getUbicacion().replace("\\", "/");
            f.setUbicacion(current.replaceFirst("^" + java.util.regex.Pattern.quote(oldPrefix), java.util.regex.Matcher.quoteReplacement(newPrefix)));
        }
        repository.saveAll(subFolders);
    }

    public List<Archivos> buscarTodos() {
        return repository.findAll();
    }

    public java.util.Optional<Archivos> buscarPorId(Long id) {
        return repository.findById(id);
    }

    @org.springframework.transaction.annotation.Transactional
    public Archivos guardar(Archivos archivo) {
        return repository.save(archivo);
    }

    @org.springframework.transaction.annotation.Transactional
    public void eliminar(Archivos doc) {
        repository.delete(doc);
    }

    public long contarDocumentos() {
        return repository.count() - repository.countFolders();
    }

    public long contarCarpetas() {
        return repository.countFolders();
    }

    public List<Archivos> buscarArchivosVisiblesPara(String email) {
        return repository.buscarArchivosVisiblesPara(email);
    }

    public List<Archivos> buscarPorCandidatoIdOEmail(Long candidatoId, String email) {
        return repository.findByCandidatoIdOrEmail(candidatoId, email);
    }

    public List<Archivos> buscarPorUbicacionPrefijo(String prefix) {
        return repository.findByUbicacionStartingWith(prefix);
    }

    public List<Archivos> buscarCarpetasPorUbicacionPrefijo(String prefix) {
        return repository.findFoldersByUbicacionStartingWith(prefix);
    }
}