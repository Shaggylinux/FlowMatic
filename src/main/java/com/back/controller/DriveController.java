package com.back.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.back.model.Archivos;
import com.back.model.Usuario;
import org.springframework.http.HttpHeaders;
import org.springframework.core.io.Resource;
import com.back.repository.ArchivosRepository;
import com.back.repository.UsuarioRepository;
import org.springframework.ui.Model;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/drive")
public class DriveController {
    
    private final String ROOT_DIR = "superfolder/";

    @Autowired
    private ArchivosRepository filesRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

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

@GetMapping
public String mostrarPagina(@RequestParam(name = "folder", required = false, defaultValue = "") String folder, 
                            java.security.Principal principal, Model model) {
    
    String loginId = (principal != null) ? principal.getName() : "ANÓNIMO";
    
    Usuario userSession = usuarioRepository.findByUsername(loginId);
    if (userSession == null) {
        userSession = usuarioRepository.findByEmail(loginId).orElse(null);
    }

    String usernameReal = (userSession != null) ? userSession.getUsername() : loginId;
    String emailReal = (userSession != null) ? userSession.getEmail() : loginId;

    List<Archivos> todos = filesRepository.buscarArchivosVisiblesPara(usernameReal);
    
    if (todos.isEmpty()) {
        todos = filesRepository.buscarArchivosVisiblesPara(emailReal);
    }

    String folderActualURL = folder.replace("\\", "/").replaceAll("^/+|/+$", "").trim();

    List<Archivos> archivosEnEstaCarpeta = todos.stream()
            .filter(a -> !a.isEsCarpeta()) 
            .filter(a -> {
                String ubicacionDB = a.getUbicacion().replace("\\", "/");
                String folderEnDB = ubicacionDB
                        .replace(ROOT_DIR.replace("\\", "/"), "")
                        .replace(a.getNombre(), "")
                        .replaceAll("^/+|/+$", "")
                        .trim();
                return folderEnDB.equalsIgnoreCase(folderActualURL);
            })
            .toList();

    model.addAttribute("usuarioActualObjeto", userSession != null ? userSession : new Usuario());
    model.addAttribute("usuarioActual", usernameReal);
    model.addAttribute("carpetas", todos.stream().filter(Archivos::isEsCarpeta).toList());
    model.addAttribute("archivos", archivosEnEstaCarpeta);
    model.addAttribute("folderActual", folderActualURL);
    model.addAttribute("listaCandidatos", usuarioRepository.findByRol("ROLE_CANDIDATO"));
    
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
                Archivos carpeta = new Archivos();
                carpeta.setNombre(nombre);
                carpeta.setUbicacion(rutaFisica.toString().replace("\\", "/")); 
                carpeta.setEsCarpeta(true);
                carpeta.setPropietario(principal.getName());
                filesRepository.save(carpeta);
            }
        } catch (IOException e) { e.printStackTrace(); }
        return "redirect:/drive?folder=" + folderDestino.replace("\\", "/");
    }

    @PostMapping("/subir-archivo")
    public String subirArchivo(@RequestParam("archivo") MultipartFile file, 
                            @RequestParam("folderDestino") String folderDestino,
                            java.security.Principal principal) {
        if (file.isEmpty()) return "redirect:/drive?folder=" + folderDestino;
        String folderLimpio = folderDestino.replace(ROOT_DIR, "").replace("\\", "/").trim();
        Path directorioFisico = Paths.get(ROOT_DIR, folderLimpio);
        try {
            if (!Files.exists(directorioFisico)) Files.createDirectories(directorioFisico);
            Path rutaArchivoFinal = directorioFisico.resolve(file.getOriginalFilename());
            Files.copy(file.getInputStream(), rutaArchivoFinal, StandardCopyOption.REPLACE_EXISTING);
            Archivos nuevoArchivo = new Archivos();
            nuevoArchivo.setNombre(file.getOriginalFilename());
            nuevoArchivo.setUbicacion(rutaArchivoFinal.toString().replace("\\", "/")); 
            nuevoArchivo.setPropietario(principal.getName());
            nuevoArchivo.setEsCarpeta(false);
            filesRepository.save(nuevoArchivo);
        } catch (IOException e) { e.printStackTrace(); }
        return "redirect:/drive?folder=" + folderLimpio;
    }

    @PostMapping("/eliminar")
    public String eliminarArchivo(@RequestParam Long archivoId, java.security.Principal principal) {
        filesRepository.findById(archivoId).ifPresent(archivo -> {
            if (archivo.getPropietario().equals(principal.getName())) {
                try {
                    Files.deleteIfExists(Paths.get(archivo.getUbicacion()));
                    filesRepository.delete(archivo);
                } catch (IOException e) { e.printStackTrace(); }
            }
        });
        return "redirect:/drive";
    }

    @GetMapping("/descargar")
    public ResponseEntity<Resource> descargarArchivo(@RequestParam Long archivoId) {
        return filesRepository.findById(archivoId).map(archivo -> {
            try {
                Path ruta = Paths.get(archivo.getUbicacion());
                Resource recurso = new UrlResource(ruta.toUri());
                if (recurso.exists() || recurso.isReadable()) {
                    return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + archivo.getNombre() + "\"")
                        .body(recurso);
                }
            } catch (MalformedURLException e) { e.printStackTrace(); }
            return ResponseEntity.notFound().<Resource>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/compartir")
    public String compartirArchivo(@RequestParam("archivoId") Long archivoId, 
                                   @RequestParam("emailDestinatario") String emailDestinatario,
                                   java.security.Principal principal) {
        filesRepository.findById(archivoId).ifPresent(archivo -> {
            if (archivo.getPropietario().equals(principal.getName())) {
                archivo.setDestinario(emailDestinatario);
                filesRepository.save(archivo);
            }
        });
        return "redirect:/drive";
    }

@PostMapping("/actualizar-estado")
    public String actualizarEstado(@RequestParam("usuarioId") Long usuarioId, 
                                   @RequestParam("nuevoEstado") String nuevoEstado) {
        
        Optional<Usuario> userOpt = usuarioRepository.findById(usuarioId);
        if (userOpt.isPresent()) {
            Usuario u = userOpt.get();
            u.setEstado(nuevoEstado);
            usuarioRepository.save(u);
        }
        
        return "redirect:/drive";
    }
}