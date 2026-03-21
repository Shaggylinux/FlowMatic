package com.back.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.ui.Model;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/")
public class DriveController {
    
    private final String ROOT_DIR = "mis_archivos/";

@GetMapping
public String mostrarPagina(@RequestParam(name = "folder", required = false, defaultValue = "") String folder, Model model) {
    Path rutaActual = Paths.get(ROOT_DIR + folder);
    
    try {
        List<ArchivoDTO> contenido = Files.list(rutaActual)
            .map(path -> new ArchivoDTO(
                path.getFileName().toString(), 
                Files.isDirectory(path)
            ))
            .collect(Collectors.toList());
        
        model.addAttribute("objetos", contenido);
        model.addAttribute("folderActual", folder);
    } catch (IOException e) {
        e.printStackTrace();
    }
    return "drive";
}

public static class ArchivoDTO {
    private String nombre;
    private boolean esCarpeta;

    public ArchivoDTO(String nombre, boolean esCarpeta) {
        this.nombre = nombre;
        this.esCarpeta = esCarpeta;
    }
    public String getNombre() { return nombre; }
    public boolean isEsCarpeta() { return esCarpeta; }
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
                           @RequestParam("folderDestino") String folderDestino) {
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
        
        System.out.println("Archivo guardado en: " + rutaArchivoFinal.toAbsolutePath());

    } catch (IOException e) {
        e.printStackTrace();
    }
    
    return "redirect:/?folder=" + folderDestino;
    }
}