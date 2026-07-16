package com.back.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.back.model.Archivos;
import com.back.model.Usuario;
import com.back.model.Candidato;
import org.springframework.http.HttpHeaders;
import org.springframework.core.io.Resource;
import com.back.repository.ArchivosRepository;
import com.back.repository.UsuarioRepository;
import com.back.repository.CandidatoRepository;
import org.springframework.ui.Model;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.*;
import java.security.Principal;

@Controller
@RequestMapping("/drive")
public class DriveController {

    private final String ROOT_DIR = "superfolder/";

    @Autowired
    private ArchivosRepository filesRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CandidatoRepository candidatoRepository;

    @jakarta.annotation.PostConstruct
    public void init() {
        try {
            Path rutaRaiz = Paths.get(ROOT_DIR);
            if (!Files.exists(rutaRaiz)) {
                Files.createDirectories(rutaRaiz);
            }
        } catch (IOException e) {
            System.err.println("Error: No se pudo crear la carpeta ra\u00edz: " + e.getMessage());
        }
    }

    @GetMapping
    public String mostrarPagina(@RequestParam(name = "folder", required = false, defaultValue = "") String folder,
            Principal principal, Model model) {
        String loginId = (principal != null) ? principal.getName() : null;
        if (loginId == null)
            return "redirect:/login";

        Usuario usuarioActual = usuarioRepository.findByEmail(loginId).orElse(null);

        String emailReal = (usuarioActual != null) ? usuarioActual.getEmail() : loginId;

        Set<Archivos> conjuntoTodo = new HashSet<>();
        List<Archivos> listaPorUser = filesRepository.buscarArchivosVisiblesPara(emailReal);
        List<Archivos> listaPorEmail = filesRepository.buscarArchivosVisiblesPara(emailReal);
        if (listaPorUser != null) conjuntoTodo.addAll(listaPorUser);
        if (listaPorEmail != null) conjuntoTodo.addAll(listaPorEmail);
        List<Archivos> todos = new ArrayList<>(conjuntoTodo);

        String folderActualURL = folder.replace("\\", "/").replaceAll("^/+|/+$", "").trim();

        final Usuario refUsuario = usuarioActual;
        List<Archivos> archivosEnEstaCarpeta = todos.stream()
                .filter(a -> !a.isEsCarpeta())
                .filter(a -> {
                    if (refUsuario == null) return false;
                    if ("ROLE_RRHH".equals(refUsuario.getRol())) {
                        String folderEnDB = a.getUbicacion().replace("\\", "/")
                                .replace(ROOT_DIR.replace("\\", "/"), "")
                                .replace(a.getNombre(), "")
                                .replaceAll("^/+|/+$", "").trim();
                        return folderEnDB.equalsIgnoreCase(folderActualURL);
                    }
                    return true;
                })
                .toList();

        Map<String, Object> usuarioData = new HashMap<>();
        if (usuarioActual != null) {
            usuarioData.put("id", usuarioActual.getId());
            usuarioData.put("email", usuarioActual.getEmail());
            usuarioData.put("rol", usuarioActual.getRol());
            usuarioData.put("activo", usuarioActual.isActivo());

            if ("ROLE_CANDIDATO".equals(usuarioActual.getRol())) {
                Candidato candidato = candidatoRepository.findById(usuarioActual.getId()).orElse(null);
                if (candidato != null) {
                    usuarioData.put("username", candidato.getUsername());
                    usuarioData.put("apellido", candidato.getApellido());
                    usuarioData.put("estado", candidato.getEstado() != null ? candidato.getEstado() : "Registrado");
                }
            }
        }

        model.addAttribute("usuarioActualObjeto", usuarioData);
        model.addAttribute("usuarioActual", loginId);
        model.addAttribute("carpetas", todos.stream().filter(Archivos::isEsCarpeta).toList());
        model.addAttribute("archivos", archivosEnEstaCarpeta);
        model.addAttribute("folderActual", folderActualURL);
        List<Candidato> candidatoList = candidatoRepository.findAll();
        List<Map<String, Object>> candidatosConEmail = new ArrayList<>();
        for (Candidato c : candidatoList) {
            Map<String, Object> cm = new HashMap<>();
            cm.put("id", c.getId());
            cm.put("username", c.getUsername());
            cm.put("apellido", c.getApellido());
            cm.put("estado", c.getEstado() != null ? c.getEstado() : "Registrado");
            usuarioRepository.findById(c.getId()).ifPresent(u -> cm.put("email", u.getEmail()));
            candidatosConEmail.add(cm);
        }
        model.addAttribute("listaCandidatos", candidatosConEmail);

        return "drive";
    }

    @PostMapping("/crear-carpeta")
    public String crearCarpeta(@RequestParam("nombre") String nombre,
                                Principal principal, Model model) {
        String loginId = (principal != null) ? principal.getName() : null;
        if (loginId == null) return "redirect:/login";

        Usuario usuarioActual = usuarioRepository.findByEmail(loginId).orElse(null);
        String email = (usuarioActual != null) ? usuarioActual.getEmail() : loginId;

        if (nombre == null || nombre.trim().isEmpty()) return "redirect:/drive";

        String rutaSinSuper = nombre.replace("\\", "/").replaceAll("^/+|/+$", "").trim();
        String rutaCarpeta = ROOT_DIR + rutaSinSuper + "/";

        try {
            Files.createDirectories(Paths.get(rutaCarpeta));
            Archivos carpeta = new Archivos();
            carpeta.setNombre(rutaSinSuper);
            carpeta.setUbicacion(rutaCarpeta);
            carpeta.setEsCarpeta(true);
            carpeta.setPropietario(email);
            filesRepository.save(carpeta);
        } catch (IOException e) {
            model.addAttribute("error", "No se pudo crear la carpeta: " + e.getMessage());
            return "redirect:/drive";
        }
        return "redirect:/drive?folder=" + nombre;
    }

    @PostMapping("/subir-archivo")
    public String subirArchivo(@RequestParam("archivo") MultipartFile archivo,
                                @RequestParam("folder") String folder,
                                Principal principal) {
        String loginId = (principal != null) ? principal.getName() : null;
        if (loginId == null) return "redirect:/login";

        Usuario usuarioActual = usuarioRepository.findByEmail(loginId).orElse(null);
        String email = (usuarioActual != null) ? usuarioActual.getEmail() : loginId;

        folder = folder.replace("\\", "/").replaceAll("^/+|/+$", "").trim();

        try {
            String rutaDestino = ROOT_DIR + (folder.isEmpty() ? "" : folder + "/") + archivo.getOriginalFilename();
            Path rutaCompleta = Paths.get(rutaDestino);
            Files.createDirectories(rutaCompleta.getParent());
            Files.copy(archivo.getInputStream(), rutaCompleta, StandardCopyOption.REPLACE_EXISTING);

            Archivos doc = new Archivos();
            doc.setNombre(archivo.getOriginalFilename());
            doc.setUbicacion(rutaDestino);
            doc.setPropietario(email);
            filesRepository.save(doc);
        } catch (IOException e) {
            return "redirect:/drive?folder=" + folder;
        }

        return "redirect:/drive?folder=" + folder;
    }

    @GetMapping("/descargar")
    public ResponseEntity<Resource> descargarArchivo(@RequestParam("fileId") Long fileId) {
        Optional<Archivos> archivoOpt = filesRepository.findById(fileId);
        if (archivoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Archivos archivo = archivoOpt.get();
        try {
            Path path = Paths.get(archivo.getUbicacion()).normalize();
            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists()) return ResponseEntity.notFound().build();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + archivo.getNombre() + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/eliminar")
    public String eliminarArchivo(@RequestParam("fileId") Long fileId,
                                   @RequestParam(value = "folder", defaultValue = "") String folder) {
        Optional<Archivos> archivoOpt = filesRepository.findById(fileId);
        if (archivoOpt.isEmpty()) return "redirect:/drive?folder=" + folder;
        Archivos archivo = archivoOpt.get();
        try {
            Path path = Paths.get(archivo.getUbicacion());
            Files.deleteIfExists(path);
        } catch (IOException ignored) {}
        filesRepository.delete(archivo);

        return "redirect:/drive?folder=" + folder;
    }

    @GetMapping("/ver-archivo/{id}")
    public ResponseEntity<Resource> verArchivo(@PathVariable Long id) {
        Optional<Archivos> archivoOpt = filesRepository.findById(id);
        if (archivoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Archivos archivo = archivoOpt.get();
        try {
            Path path = Paths.get(archivo.getUbicacion()).normalize();
            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists()) return ResponseEntity.notFound().build();
            String contentType = Files.probeContentType(path);
            if (contentType == null) contentType = "application/octet-stream";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + archivo.getNombre() + "\"")
                    .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
