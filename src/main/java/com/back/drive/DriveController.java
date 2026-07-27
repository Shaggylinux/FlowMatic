package com.back.drive;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.back.auth.Usuario;
import com.back.candidatos.Candidato;
import org.springframework.http.HttpHeaders;
import org.springframework.core.io.Resource;
import com.back.auth.UsuarioRepository;
import com.back.candidatos.CandidatoRepository;
import com.back.notificaciones.NotificacionService;
import com.back.shared.Sanitizer;
import org.springframework.ui.Model;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.security.Principal;
import java.time.LocalDateTime;

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

    @Autowired
    private FilesServices filesServices;

    @Autowired
    private NotificacionService notificacionService;

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
                                @RequestParam(value = "folder", defaultValue = "") String folder,
                                Principal principal, Model model) {
        String loginId = (principal != null) ? principal.getName() : null;
        if (loginId == null) return "redirect:/login";

        Usuario usuarioActual = usuarioRepository.findByEmail(loginId).orElse(null);
        String email = (usuarioActual != null) ? usuarioActual.getEmail() : loginId;

        if (nombre == null || nombre.trim().isEmpty()) return "redirect:/drive";

        folder = Sanitizer.sanitizePath(folder);
        String rutaSinSuper = Sanitizer.sanitizePath(nombre);
        if (rutaSinSuper.isEmpty()) return "redirect:/drive";

        try {
            filesServices.crearCarpetaDrive(rutaSinSuper, folder, email);
        } catch (IOException e) {
            model.addAttribute("error", "No se pudo crear la carpeta: " + e.getMessage());
            return "redirect:/drive";
        }
        String rutaRelativa = folder.isEmpty() ? rutaSinSuper : folder + "/" + rutaSinSuper;
        return "redirect:/drive?folder=" + rutaRelativa;
    }

    @PostMapping("/subir-archivo")
    public String subirArchivo(@RequestParam("archivo") MultipartFile archivo,
                                @RequestParam("folder") String folder,
                                Principal principal) {
        String loginId = (principal != null) ? principal.getName() : null;
        if (loginId == null) return "redirect:/login";

        Usuario usuarioActual = usuarioRepository.findByEmail(loginId).orElse(null);
        String email = (usuarioActual != null) ? usuarioActual.getEmail() : loginId;

        folder = Sanitizer.sanitizePath(folder);
        String filename = archivo.getOriginalFilename();
        if (!Sanitizer.isValidFileName(filename)) {
            return "redirect:/drive?folder=" + folder;
        }

        try {
            filesServices.subirArchivoDrive(archivo, folder, email, filename);
        } catch (IOException e) {
            return "redirect:/drive?folder=" + folder;
        }

        return "redirect:/drive?folder=" + folder;
    }

    private boolean esPropietarioODestinatario(Archivos archivo, String email) {
        if (email == null) return false;
        return email.equalsIgnoreCase(archivo.getPropietario())
            || (archivo.getDestinario() != null && email.equalsIgnoreCase(archivo.getDestinario()));
    }

    @GetMapping("/descargar")
    public ResponseEntity<Resource> descargarArchivo(@RequestParam("fileId") Long fileId,
                                                      Principal principal) {
        String email = principal != null ? principal.getName() : null;
        Optional<Archivos> archivoOpt = filesRepository.findById(fileId);
        if (archivoOpt.isEmpty() || !esPropietarioODestinatario(archivoOpt.get(), email)) {
            return ResponseEntity.notFound().build();
        }
        Archivos archivo = archivoOpt.get();
        try {
            Resource resource = new UrlResource(filesServices.obtenerRutaArchivo(archivo).toUri());
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
                                   @RequestParam(value = "folder", defaultValue = "") String folder,
                                   Principal principal) {
        String email = principal != null ? principal.getName() : null;
        Optional<Archivos> archivoOpt = filesRepository.findById(fileId);
        if (archivoOpt.isEmpty() || email == null) return "redirect:/drive?folder=" + folder;
        Archivos archivo = archivoOpt.get();
        if (!email.equalsIgnoreCase(archivo.getPropietario())) return "redirect:/drive?folder=" + folder;
        
        filesServices.eliminarArchivo(archivo);

        return "redirect:/drive?folder=" + folder;
    }

    @PostMapping("/compartir")
    public String compartirArchivo(@RequestParam("archivoId") Long archivoId,
                                    @RequestParam("emailDestinatario") String destinatario,
                                    Principal principal) {
        String email = principal != null ? principal.getName() : null;
        Optional<Archivos> archivoOpt = filesRepository.findById(archivoId);
        if (archivoOpt.isEmpty() || email == null) return "redirect:/drive";
        if (!email.equalsIgnoreCase(archivoOpt.get().getPropietario())) return "redirect:/drive";
        filesServices.compartirArchivo(archivoId, destinatario);
        return "redirect:/drive";
    }

    @PostMapping("/actualizar-estado")
    public String actualizarEstado(@RequestParam("usuarioId") Long id,
                                    @RequestParam("nuevoEstado") String estado) {
        Candidato candidato = candidatoRepository.findById(id).orElse(null);
        if (candidato == null) return "redirect:/drive";

        String estadoAnterior = candidato.getEstado();
        candidato.setEstado(estado);
        candidato.setUltimaActualizacion(LocalDateTime.now());
        candidatoRepository.save(candidato);

        if (estadoAnterior == null || !estadoAnterior.equals(estado)) {
            String nombre = candidato.getUsername() + " " + (candidato.getApellido() != null ? candidato.getApellido() : "");
            notificacionService.crear("ESTADO",
                "Estado actualizado: " + nombre + " ahora como \"" + estado + "\"",
                id, nombre, "/gestion-candidatos");
        }

        return "redirect:/drive";
    }

    @GetMapping("/ver-archivo/{id}")
    public ResponseEntity<Resource> verArchivo(@PathVariable Long id, Principal principal) {
        String email = principal != null ? principal.getName() : null;
        Optional<Archivos> archivoOpt = filesRepository.findById(id);
        if (archivoOpt.isEmpty() || !esPropietarioODestinatario(archivoOpt.get(), email)) {
            return ResponseEntity.notFound().build();
        }
        Archivos archivo = archivoOpt.get();
        try {
            java.nio.file.Path path = filesServices.obtenerRutaArchivo(archivo);
            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists()) return ResponseEntity.notFound().build();
            String contentType = java.nio.file.Files.probeContentType(path);
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
