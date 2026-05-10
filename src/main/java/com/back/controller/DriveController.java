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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
        String loginId = (principal != null) ? principal.getName() : null;
        if (loginId == null)
            return "redirect:/login";

        Usuario userSession = usuarioRepository.findByUsername(loginId);
        if (userSession == null) {
            userSession = usuarioRepository.findByEmail(loginId).orElse(null);
        }

        String usernameReal = (userSession != null) ? userSession.getUsername() : loginId;
        String emailReal = (userSession != null) ? userSession.getEmail() : loginId;

        List<Archivos> listaPorUser = filesRepository.buscarArchivosVisiblesPara(usernameReal);
        List<Archivos> listaPorEmail = filesRepository.buscarArchivosVisiblesPara(emailReal);

        Set<Archivos> conjuntoTodo = new HashSet<>();
        if (listaPorUser != null)
            conjuntoTodo.addAll(listaPorUser);
        if (listaPorEmail != null)
            conjuntoTodo.addAll(listaPorEmail);
        List<Archivos> todos = new ArrayList<>(conjuntoTodo);

        String folderActualURL = folder.replace("\\", "/").replaceAll("^/+|/+$", "").trim();

        Usuario tempUser = usuarioRepository.findByUsername(loginId);
        if (tempUser == null) {
            tempUser = usuarioRepository.findByEmail(loginId).orElse(null);
        }

        final Usuario usuarioParaFiltro = tempUser;
        if (listaPorUser != null)
            conjuntoTodo.addAll(listaPorUser);
        if (listaPorEmail != null)
            conjuntoTodo.addAll(listaPorEmail);

        List<Archivos> archivosEnEstaCarpeta = todos.stream()
                .filter(a -> !a.isEsCarpeta())
                .filter(a -> {
                    if (usuarioParaFiltro == null)
                        return false;

                    if ("ROLE_RRHH".equals(usuarioParaFiltro.getRol())) {
                        String ubicacionDB = a.getUbicacion().replace("\\", "/");
                        String folderEnDB = ubicacionDB
                                .replace(ROOT_DIR.replace("\\", "/"), "")
                                .replace(a.getNombre(), "")
                                .replaceAll("^/+|/+$", "")
                                .trim();
                        return folderEnDB.equalsIgnoreCase(folderActualURL);
                    }

                    return true;
                })
                .toList();

        model.addAttribute("usuarioActualObjeto", usuarioParaFiltro != null ? usuarioParaFiltro : new Usuario());
        model.addAttribute("usuarioActual", loginId);
        model.addAttribute("carpetas", todos.stream().filter(Archivos::isEsCarpeta).toList());
        model.addAttribute("archivos", archivosEnEstaCarpeta);
        model.addAttribute("folderActual", folderActualURL);
        model.addAttribute("listaCandidatos", usuarioRepository.findByRol("ROLE_CANDIDATO"));

        model.addAttribute("usuarioActualObjeto", userSession != null ? userSession : new Usuario());
        model.addAttribute("usuarioActual", loginId);
        model.addAttribute("carpetas", todos.stream().filter(Archivos::isEsCarpeta).toList());
        model.addAttribute("archivos", archivosEnEstaCarpeta);
        model.addAttribute("folderActual", folderActualURL);
        model.addAttribute("listaCandidatos", usuarioRepository.findByRol("ROLE_CANDIDATO"));

        return "drive";
    }

    String usernameReal = (userSession != null) ? userSession.getUsername() : loginId;
    String emailReal = (userSession != null) ? userSession.getEmail() : loginId;

    List<Archivos> listaPorUser = filesRepository.buscarArchivosVisiblesPara(usernameReal);
    List<Archivos> listaPorEmail = filesRepository.buscarArchivosVisiblesPara(emailReal);
    
    Set<Archivos> conjuntoTodo = new HashSet<>();
    if (listaPorUser != null) conjuntoTodo.addAll(listaPorUser);
    if (listaPorEmail != null) conjuntoTodo.addAll(listaPorEmail);
    List<Archivos> todos = new ArrayList<>(conjuntoTodo);

    String folderActualURL = folder.replace("\\", "/").replaceAll("^/+|/+$", "").trim();


    Usuario tempUser = usuarioRepository.findByUsername(loginId);
    if (tempUser == null) {
        tempUser = usuarioRepository.findByEmail(loginId).orElse(null);
    }
    
    final Usuario usuarioParaFiltro = tempUser;
    if (listaPorUser != null) conjuntoTodo.addAll(listaPorUser);
    if (listaPorEmail != null) conjuntoTodo.addAll(listaPorEmail);

    List<Archivos> archivosEnEstaCarpeta = todos.stream()
            .filter(a -> !a.isEsCarpeta()) 
            .filter(a -> {
                if (usuarioParaFiltro == null) return false; 

                if ("ROLE_RRHH".equals(usuarioParaFiltro.getRol())) {
                    String ubicacionDB = a.getUbicacion().replace("\\", "/");
                    String folderEnDB = ubicacionDB
                            .replace(ROOT_DIR.replace("\\", "/"), "")
                            .replace(a.getNombre(), "")
                            .replaceAll("^/+|/+$", "")
                            .trim();
                    return folderEnDB.equalsIgnoreCase(folderActualURL);
                }
                
                if ("ROLE_CANDIDATO".equals(usuarioParaFiltro.getRol())) {
                    return a.getPropietario() != null && 
                           (a.getPropietario().equals(usuarioParaFiltro.getUsername()) || 
                            a.getPropietario().equals(usuarioParaFiltro.getEmail()) ||
                            a.getDestinario() != null && a.getDestinario().equals(usuarioParaFiltro.getEmail()));
                }
                
                return true;
            })
            .toList();

    model.addAttribute("usuarioActualObjeto", usuarioParaFiltro != null ? usuarioParaFiltro : new Usuario());
    model.addAttribute("usuarioActual", loginId);
    model.addAttribute("carpetas", todos.stream().filter(Archivos::isEsCarpeta).toList());
    model.addAttribute("archivos", archivosEnEstaCarpeta);
    model.addAttribute("folderActual", folderActualURL);
    model.addAttribute("listaCandidatos", usuarioRepository.findByRol("ROLE_CANDIDATO"));

    model.addAttribute("usuarioActualObjeto", userSession != null ? userSession : new Usuario());
    model.addAttribute("usuarioActual", loginId);
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "redirect:/drive?folder=" + folderDestino.replace("\\", "/");
    }

    @PostMapping("/subir-archivo")
    public String subirArchivo(@RequestParam("archivo") MultipartFile file,
            @RequestParam("folderDestino") String folderDestino,
            java.security.Principal principal) {
        if (file.isEmpty())
            return "redirect:/drive?folder=" + folderDestino;
        String folderLimpio = folderDestino.replace(ROOT_DIR, "").replace("\\", "/").trim();
        Path directorioFisico = Paths.get(ROOT_DIR, folderLimpio);
        try {
            if (!Files.exists(directorioFisico))
                Files.createDirectories(directorioFisico);
            Path rutaArchivoFinal = directorioFisico.resolve(file.getOriginalFilename());
            Files.copy(file.getInputStream(), rutaArchivoFinal, StandardCopyOption.REPLACE_EXISTING);
            Archivos nuevoArchivo = new Archivos();
            nuevoArchivo.setNombre(file.getOriginalFilename());
            nuevoArchivo.setUbicacion(rutaArchivoFinal.toString().replace("\\", "/"));
            nuevoArchivo.setPropietario(principal.getName());
            nuevoArchivo.setEsCarpeta(false);
            filesRepository.save(nuevoArchivo);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "redirect:/drive?folder=" + folderLimpio;
    }

    @PostMapping("/eliminar")
    public String eliminarArchivo(@RequestParam Long archivoId, java.security.Principal principal) {
        filesRepository.findById(archivoId).ifPresent(archivo -> {
            if (archivo.getPropietario().equals(principal.getName())) {
                try {
                    Files.deleteIfExists(Paths.get(archivo.getUbicacion()));
                    filesRepository.delete(archivo);
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
                            .header(HttpHeaders.CONTENT_DISPOSITION,
                                    "attachment; filename=\"" + archivo.getNombre() + "\"")
                            .body(recurso);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
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

    @GetMapping("/ver-archivo/{archivoId}")
    public ResponseEntity<Resource> previsualizar(@PathVariable Long archivoId) {
        return filesRepository.findById(archivoId).map(archivo -> {
            try {
                Path path = Paths.get(archivo.getUbicacion());
                Resource recurso = new UrlResource(path.toUri());

                if (recurso.exists() || recurso.isReadable()) {
                    String contentType = Files.probeContentType(path);
                    if (contentType == null) {
                        contentType = "application/octet-stream";
                    }

                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_TYPE, contentType)
                            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + archivo.getNombre() + "\"")
                            .body(recurso);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return ResponseEntity.notFound().<Resource>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}