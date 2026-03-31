package com.back.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import com.back.model.Archivos;
import org.springframework.http.HttpHeaders;
import org.springframework.core.io.Resource;
import com.back.repository.FilesRepository;
import org.springframework.ui.Model;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/")
public class DriveController {
    
    private final String ROOT_DIR = "superfolder/";

    @jakarta.annotation.PostConstruct
    public void init() {
        try {
            Path rutaRaiz = Paths.get(ROOT_DIR);
            if (!Files.exists(rutaRaiz)) {
                Files.createDirectories(rutaRaiz);
            }
        } catch (IOException e) {
            System.err.println("Error: No se pudo crear la carpeta raíz: " + e.getMessage());
        }
    }
    
    @Autowired
    private FilesRepository filesRepository;

@GetMapping
public String mostrarPagina(@RequestParam(name = "folder", required = false, defaultValue = "") String folder, 
                            java.security.Principal principal, Model model) {
    String nombreUsuario = (principal != null) ? principal.getName() : "";
    List<Archivos> todos = filesRepository.buscarArchivosVisiblesPara(nombreUsuario);

    // Normalizamos el folder que viene por URL de una vez
    String folderActualURL = folder.replace("\\", "/").replaceAll("^/+|/+$", "").trim();

    List<Archivos> archivosEnEstaCarpeta = todos.stream()
            .filter(a -> !a.isEsCarpeta()) 
            .filter(a -> {
                // 1. Normalizamos la ruta de la DB (Windows \ -> Web /)
                String ubicacionDB = a.getUbicacion().replace("\\", "/");
                
                // 2. Quitamos el superfolder/ y el nombre del archivo
                String folderEnDB = ubicacionDB
                        .replace(ROOT_DIR.replace("\\", "/"), "")
                        .replace(a.getNombre(), "")
                        .replaceAll("^/+|/+$", "") // Quitamos barras al inicio/final
                        .trim();

                // DEBUG para que veas el cambio en consola:
                // System.out.println("Comparando DB: [" + folderEnDB + "] con URL: [" + folderActualURL + "]");

                return folderEnDB.equalsIgnoreCase(folderActualURL);
            })
            .toList();

    model.addAttribute("carpetas", todos.stream().filter(Archivos::isEsCarpeta).toList());
    model.addAttribute("archivos", archivosEnEstaCarpeta);
    model.addAttribute("usuarioActual", nombreUsuario);
    model.addAttribute("folderActual", folderActualURL); // Mandamos la versión limpia a la vista
    
    return "drive";
}

@PostMapping("/crear-carpeta")
public String crearCarpeta(@RequestParam("nombre") String nombre, 
                           @RequestParam("folderDestino") String folderDestino,
                           java.security.Principal principal) {
    Path rutaFisica = Paths.get(ROOT_DIR, nombre);

    try {
        if (!Files.exists(rutaFisica)) {
            Files.createDirectories(rutaFisica);
            
            Archivos carpeta = new Archivos(); // Tu variable se llama carpeta
            carpeta.setNombre(nombre);
            
            // AGREGA ESTO: Forzamos la barra normal para la DB
            carpeta.setUbicacion(rutaFisica.toString().replace("\\", "/")); 
            
            carpeta.setEsCarpeta(true);
            carpeta.setPropietario(principal.getName());
            
            filesRepository.save(carpeta);
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
    return "redirect:/?folder=" + folderDestino.replace("\\", "/");
}

@PostMapping("/subir-archivo")
public String subirArchivo(@RequestParam("archivo") MultipartFile file, 
                           @RequestParam("folderDestino") String folderDestino,
                           java.security.Principal principal) {
    if (file.isEmpty()) return "redirect:/?folder=" + folderDestino;

    String folderLimpio = folderDestino.replace(ROOT_DIR, "").replace("\\", "/").trim();
    Path directorioFisico = Paths.get(ROOT_DIR, folderLimpio);

    try {
        if (!Files.exists(directorioFisico)) {
            Files.createDirectories(directorioFisico);
        }
        
        Path rutaArchivoFinal = directorioFisico.resolve(file.getOriginalFilename());
        Files.copy(file.getInputStream(), rutaArchivoFinal, StandardCopyOption.REPLACE_EXISTING);

        Archivos nuevoArchivo = new Archivos(); // Tu variable aquí es nuevoArchivo
        nuevoArchivo.setNombre(file.getOriginalFilename());
        
        // AGREGA ESTO: Limpiamos la ruta antes de guardar en la DB
        nuevoArchivo.setUbicacion(rutaArchivoFinal.toString().replace("\\", "/")); 
        
        nuevoArchivo.setPropietario(principal.getName());
        nuevoArchivo.setEsCarpeta(false);
        
        filesRepository.save(nuevoArchivo);
    } catch (IOException e) {
        e.printStackTrace();
    }
    return "redirect:/?folder=" + folderLimpio;
}

    @PostMapping("/eliminar")
    public String eliminarArchivo(@RequestParam Long archivoId, java.security.Principal principal) {
        Optional<Archivos> archivoOpt = filesRepository.findById(archivoId);
        if (archivoOpt.isPresent()) {
            Archivos archivo = archivoOpt.get();
            if (archivo.getPropietario().equals(principal.getName())) {
                try {
                    Files.deleteIfExists(Paths.get(archivo.getUbicacion()));
                    filesRepository.delete(archivo);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return "redirect:/";
    }

    @GetMapping("/descargar")
    public ResponseEntity<Resource> descargarArchivo(@RequestParam Long archivoId) {
        Optional<Archivos> archivoOpt = filesRepository.findById(archivoId);
        if (archivoOpt.isPresent()) {
            Archivos archivo = archivoOpt.get();
            try {
                Path ruta = Paths.get(archivo.getUbicacion());
                Resource recurso = new UrlResource(ruta.toUri());
                if (recurso.exists() || recurso.isReadable()) {
                    return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + archivo.getNombre() + "\"")
                        .body(recurso);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return ResponseEntity.notFound().build();
    }
}