package com.back.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import com.back.model.Archivos;

import com.back.repository.FilesRepository;

import org.springframework.ui.Model;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/")
public class DriveController {
    
    private final String ROOT_DIR = "mis_archivos/";
    @Autowired
    private FilesRepository filesRepository;

@GetMapping
public String mostrarPagina(java.security.Principal principal, Model model) {
    if (principal == null) return "redirect:/login";
    
    String nombreUsuario = principal.getName();
    System.out.println("DEBUG: El usuario logueado es: " + nombreUsuario);

    List<Archivos> archivosVisibles = filesRepository.buscarArchivosVisiblesPara(nombreUsuario);
    
    System.out.println("DEBUG: Archivos encontrados: " + archivosVisibles.size());
    
    model.addAttribute("objetos", archivosVisibles);
    return "drive";
}

@PostMapping("/crear-carpeta")
public String crearCarpeta(@RequestParam("nombre") String nombre, 
                           @RequestParam("folderDestino") String folderDestino) {
    Path ruta = Paths.get(ROOT_DIR + folderDestino, nombre);
    try {
        Files.createDirectories(ruta);
    } catch (IOException e) {
        e.printStackTrace();
    }
    return "redirect:/?folder=" + folderDestino;
}

@PostMapping("/subir-archivo")
public String subirArchivo(@RequestParam("archivo") MultipartFile file, 
                           @RequestParam("folderDestino") String folderDestino,
                           java.security.Principal principal) {
    if (file.isEmpty()) {
        return "redirect:/?folder=" + folderDestino;
    }

    try {
        Path directorioDestino = Paths.get(ROOT_DIR, folderDestino);
        if (!Files.exists(directorioDestino)) {
            Files.createDirectories(directorioDestino);
        }
        Path rutaArchivoFinal = directorioDestino.resolve(file.getOriginalFilename());
        Files.copy(file.getInputStream(), rutaArchivoFinal, StandardCopyOption.REPLACE_EXISTING);

        Archivos nuevoArchivo = new Archivos();
        nuevoArchivo.setNombre(file.getOriginalFilename());
        nuevoArchivo.setUbicacion(rutaArchivoFinal.toString());
        
        String nombreUsuario = (principal != null) ? principal.getName() : "UsuarioAnonimo";
        nuevoArchivo.setPropietario(nombreUsuario);
        
        nuevoArchivo.setDestinario(null); 

        filesRepository.save(nuevoArchivo);

        System.out.println("Archivo registrado en DB para el usuario: " + nombreUsuario);

    } catch (IOException e) {
        e.printStackTrace();
    }
    
    return "redirect:/?folder=" + folderDestino;
}

@PostMapping("/compartir")
public String compartirArchivo(@RequestParam Long archivoId, @RequestParam String correoDestino) {
    Optional<Archivos> archivoOpt = filesRepository.findById(archivoId);
    
    if (archivoOpt.isPresent()) {
        Archivos archivo = archivoOpt.get();
        archivo.setDestinario(correoDestino);
        filesRepository.save(archivo);
    }
    
    return "redirect:/"; 
}
}