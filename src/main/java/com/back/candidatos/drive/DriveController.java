package com.back.candidatos.drive;

import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import com.back.auth.Usuario;
import com.back.auth.UsuarioRepository;
import com.back.candidatos.Candidato;
import com.back.drive.Archivos;
import com.back.drive.ArchivosRepository;
import com.back.drive.FilesServices;
import org.springframework.http.HttpHeaders;
import org.springframework.core.io.Resource;
import com.back.candidatos.CandidatoRepository;
import com.back.candidatos.CandidatoService;
import com.back.notificaciones.NotificacionService;
import com.back.util.Sanitizer;
import org.springframework.ui.Model;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.stream.Collectors;
import java.security.Principal;
import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/drive")
@RequiredArgsConstructor
public class DriveController {

    private final String ROOT_DIR = "superfolder/";

    private final ArchivosRepository filesRepository;
    private final UsuarioRepository usuarioRepository;
    private final CandidatoRepository candidatoRepository;
    private final FilesServices filesServices;
    private final NotificacionService notificacionService;
    private final com.back.shared.HistorialService historialService;

    @GetMapping
    public String mostrarPagina(@RequestParam(name = "folder", required = false, defaultValue = "") String folder,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "buscar", required = false) String buscar,
            @RequestParam(name = "tipo", required = false) String tipo,
            @RequestParam(name = "estado", required = false) String estado,
            @RequestParam(name = "categoria", required = false) String categoria,
            @RequestParam(name = "tab", required = false) String tab,
            @RequestParam(name = "error", required = false) String error,
            @RequestParam(name = "estadoError", required = false) String estadoError,
            @RequestParam(name = "compartido", required = false) String compartido,
            Principal principal, Model model) {
        String loginId = (principal != null) ? principal.getName() : null;
        if (loginId == null)
            return "redirect:/login";

        model.addAttribute("error", error);
        model.addAttribute("estadoError", estadoError);
        model.addAttribute("compartido", compartido);

        Usuario usuarioActual = usuarioRepository.findByEmail(loginId).orElse(null);

        String emailReal = (usuarioActual != null) ? usuarioActual.getEmail() : loginId;

        Set<Archivos> conjuntoTodo = new HashSet<>();
        if (usuarioActual != null && "ROLE_RRHH".equals(usuarioActual.getRol())) {
            List<Archivos> todosArchivos = filesRepository.findAll();
            List<Candidato> todosCandidatos = candidatoRepository.findAll();

            Map<Long, String> rrhhByCandId = new HashMap<>();
            Map<String, String> rrhhByCandEmail = new HashMap<>();

            List<Long> candIds = todosCandidatos.stream().map(Candidato::getId).toList();
            Map<Long, Usuario> candUsers = usuarioRepository.findAllById(candIds).stream()
                    .collect(Collectors.toMap(Usuario::getId, u -> u));

            for (Candidato c : todosCandidatos) {
                if (c.getRrhhEmail() != null && !c.getRrhhEmail().isBlank()) {
                    rrhhByCandId.put(c.getId(), c.getRrhhEmail().toLowerCase());
                    Usuario u = candUsers.get(c.getId());
                    if (u != null && u.getEmail() != null) {
                        rrhhByCandEmail.put(u.getEmail().toLowerCase(), c.getRrhhEmail().toLowerCase());
                    }
                }
            }

            for (Archivos a : todosArchivos) {
                String prop = (a.getPropietario() != null) ? a.getPropietario().toLowerCase() : "";
                String dest = (a.getDestinario() != null) ? a.getDestinario().toLowerCase() : "";
                String loggedEmailLower = emailReal.toLowerCase();

                String assignedRrhh = null;
                if (a.getCandidato() != null && rrhhByCandId.containsKey(a.getCandidato().getId())) {
                    assignedRrhh = rrhhByCandId.get(a.getCandidato().getId());
                } else if (!prop.isEmpty() && rrhhByCandEmail.containsKey(prop)) {
                    assignedRrhh = rrhhByCandEmail.get(prop);
                }

                if (assignedRrhh != null) {
                    if (assignedRrhh.equals(loggedEmailLower) || prop.equals(loggedEmailLower) || dest.equals(loggedEmailLower)) {
                        conjuntoTodo.add(a);
                    }
                } else {
                    conjuntoTodo.add(a);
                }
            }
        } else {
            List<Archivos> lista = filesRepository.buscarArchivosVisiblesPara(emailReal);
            if (lista != null)
                conjuntoTodo.addAll(lista);
            if (usuarioActual != null && usuarioActual.getEmail() != null && !usuarioActual.getEmail().equalsIgnoreCase(emailReal)) {
                List<Archivos> listaAlt = filesRepository.buscarArchivosVisiblesPara(usuarioActual.getEmail());
                if (listaAlt != null)
                    conjuntoTodo.addAll(listaAlt);
            }
        }
        List<Archivos> todos = new ArrayList<>(conjuntoTodo);

        String folderActualURL = folder.replace("\\", "/").replaceAll("^/+|/+$", "").trim();

        final Usuario refUsuario = usuarioActual;
        List<Archivos> archivosEnEstaCarpeta = todos.stream()
                .filter(a -> !a.isEsCarpeta())
                .filter(a -> {
                    if (refUsuario == null)
                        return false;
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

        List<Archivos> archivosFiltrados = archivosEnEstaCarpeta.stream()
                .filter(a -> {
                    if (buscar == null || buscar.isBlank())
                        return true;
                    String q = buscar.trim().toLowerCase();
                    String nombreCandidato = a.getNombreCandidatoStr();
                    return a.getNombre().toLowerCase().contains(q)
                            || (nombreCandidato != null && nombreCandidato.toLowerCase().contains(q));
                })
                .filter(a -> {
                    if (tipo == null || tipo.isBlank())
                        return true;
                    String ext = a.getNombre().contains(".")
                            ? a.getNombre().substring(a.getNombre().lastIndexOf('.') + 1).toLowerCase()
                            : "";
                    return List.of(tipo.split(",")).contains(ext);
                })
                .filter(a -> {
                    if (estado == null || estado.isBlank())
                        return true;
                    return estado.trim().equalsIgnoreCase(a.getEstadoDocumento());
                })
                .filter(a -> {
                    if (categoria == null || categoria.isBlank() || "todos".equalsIgnoreCase(categoria))
                        return true;
                    if ("requeridos".equalsIgnoreCase(categoria) || "requerido".equalsIgnoreCase(categoria)) {
                        return "Requerido".equalsIgnoreCase(a.getCategoriaCalculada());
                    }
                    if ("opcionales".equalsIgnoreCase(categoria) || "opcional".equalsIgnoreCase(categoria)) {
                        return "Opcional".equalsIgnoreCase(a.getCategoriaCalculada());
                    }
                    if ("compartidos".equalsIgnoreCase(categoria)) {
                        if (a.getDestinario() == null) return false;
                        String d = a.getDestinario().trim();
                        return d.equalsIgnoreCase(emailReal) || (usuarioActual != null && usuarioActual.getEmail() != null && d.equalsIgnoreCase(usuarioActual.getEmail().trim()));
                    }
                    return true;
                })
                .toList();

        long totalItems = archivosFiltrados.size();
        int totalPages = (int) Math.max(1, Math.ceil((double) totalItems / Math.max(size, 1)));
        if (page < 0)
            page = 0;
        if (page >= totalPages)
            page = totalPages - 1;
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        int fromIndex = Math.min(Math.toIntExact(pageable.getOffset()), archivosFiltrados.size());
        int toIndex = Math.min(fromIndex + pageable.getPageSize(), archivosFiltrados.size());
        List<Archivos> archivosPagina = archivosFiltrados.subList(fromIndex, toIndex);
        Page<Archivos> archivosPage = new PageImpl<>(archivosPagina, pageable, totalItems);

        int startItem = totalItems == 0 ? 0 : (int) pageable.getOffset() + 1;
        int endItem = (int) Math.min(pageable.getOffset() + archivosPagina.size(), totalItems);

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
                } else {
                    usuarioData.put("username", usuarioActual.getEmail());
                    usuarioData.put("apellido", "");
                    usuarioData.put("estado", "Registrado");
                }
            }
        }

        long totalSizeBytes = 0;
        int pendingCount = 0;
        int approvedCount = 0;
        int rejectedCount = 0;
        int requeridosCount = 0;
        int opcionalesCount = 0;
        int compartidosCount = 0;
        int filesCount = 0;
        int foldersCount = 0;
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM yyyy", new java.util.Locale("es", "ES"));
        sdf.setTimeZone(java.util.TimeZone.getTimeZone(java.time.ZoneId.of("America/Bogota")));

        for (Archivos a : todos) {
            if (a.isEsCarpeta()) {
                foldersCount++;
                continue;
            }
            filesCount++;
            if ("Pendiente".equalsIgnoreCase(a.getEstadoDocumento())) {
                pendingCount++;
            } else if ("Aprobado".equalsIgnoreCase(a.getEstadoDocumento())) {
                approvedCount++;
            } else if ("Rechazado".equalsIgnoreCase(a.getEstadoDocumento())) {
                rejectedCount++;
            }

            if ("Opcional".equalsIgnoreCase(a.getCategoriaCalculada())) {
                opcionalesCount++;
            } else {
                requeridosCount++;
            }

            if (a.getDestinario() != null && (a.getDestinario().trim().equalsIgnoreCase(emailReal) || (usuarioActual != null && usuarioActual.getEmail() != null && a.getDestinario().trim().equalsIgnoreCase(usuarioActual.getEmail().trim())))) {
                compartidosCount++;
            }

            java.io.File physicalFile = new java.io.File(a.getUbicacion());
            long lastModified = 0L;
            if (physicalFile.exists()) {
                long fileSize = physicalFile.length();
                totalSizeBytes += fileSize;
                String sizeStr;
                if (fileSize < 1024) sizeStr = fileSize + " B";
                else if (fileSize < 1024 * 1024) sizeStr = (fileSize / 1024) + " KB";
                else if (fileSize < 1024 * 1024 * 1024) sizeStr = String.format(java.util.Locale.US, "%.1f MB", (double)fileSize / (1024 * 1024));
                else sizeStr = String.format(java.util.Locale.US, "%.2f GB", (double)fileSize / (1024 * 1024 * 1024));
                a.setTamanoFormateado(sizeStr);
                lastModified = physicalFile.lastModified();
            }

            java.time.ZoneId zona = java.time.ZoneId.of("America/Bogota");
            java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("hh:mm a", new java.util.Locale("es", "ES"));
            timeFormat.setTimeZone(java.util.TimeZone.getTimeZone(zona));
            java.time.LocalDate hoy = java.time.LocalDate.now(zona);

            if (a.getFechaSubida() != null) {
                java.time.LocalDate fechaLocal = a.getFechaSubida().toLocalDate();
                java.util.Date cuando = java.util.Date.from(a.getFechaSubida().atZone(zona).toInstant());
                if (hoy.equals(fechaLocal)) {
                    a.setFechaModificacion("Hoy, " + timeFormat.format(cuando));
                } else if (hoy.minusDays(1).equals(fechaLocal)) {
                    a.setFechaModificacion("Ayer, " + timeFormat.format(cuando));
                } else {
                    a.setFechaModificacion(sdf.format(cuando));
                }
            } else if (physicalFile.exists()) {
                java.time.LocalDate fechaLocal = java.time.LocalDate.ofInstant(java.time.Instant.ofEpochMilli(lastModified), zona);
                if (hoy.equals(fechaLocal)) {
                    a.setFechaModificacion("Hoy, " + timeFormat.format(new java.util.Date(lastModified)));
                } else if (hoy.minusDays(1).equals(fechaLocal)) {
                    a.setFechaModificacion("Ayer, " + timeFormat.format(new java.util.Date(lastModified)));
                } else {
                    a.setFechaModificacion(sdf.format(new java.util.Date(lastModified)));
                }
            }
            
            if (a.getCandidato() != null) {
                Candidato c = candidatoRepository.findById(a.getCandidato().getId()).orElse(null);
                if (c != null && c.getUsername() != null) {
                    a.setNombreCandidatoStr(c.getUsername() + " " + (c.getApellido() != null ? c.getApellido() : ""));
                } else {
                    a.setNombreCandidatoStr(a.getCandidato().getEmail());
                }
            } else {
                Usuario propietarioUsuario = usuarioRepository.findByEmail(a.getPropietario()).orElse(null);
                if (propietarioUsuario != null && "ROLE_CANDIDATO".equals(propietarioUsuario.getRol())) {
                    Candidato c = candidatoRepository.findById(propietarioUsuario.getId()).orElse(null);
                    if (c != null && c.getUsername() != null) {
                        a.setNombreCandidatoStr(c.getUsername() + " " + (c.getApellido() != null ? c.getApellido() : ""));
                    } else {
                        a.setNombreCandidatoStr(propietarioUsuario.getEmail());
                    }
                } else {
                    String folderNombre = extraerCarpetaContenedora(a.getUbicacion());
                    a.setNombreCandidatoStr(folderNombre != null ? folderNombre : "General");
                }
            }
        }
        java.io.File superFolderFile = new java.io.File("superfolder");
        if (!superFolderFile.exists()) {
            superFolderFile.mkdirs();
        }
        long totalDiskBytes = superFolderFile.getTotalSpace();
        if (totalDiskBytes <= 0) {
            totalDiskBytes = 5L * 1024 * 1024 * 1024;
        }

        double gbUsed = (double) totalSizeBytes / (1024 * 1024 * 1024);
        double pctUsed = Math.min(((double) totalSizeBytes / totalDiskBytes) * 100.0, 100.0);

        List<Archivos> carpetasFlat = todos.stream()
                .filter(Archivos::isEsCarpeta)
                .filter(a -> !a.getNombre().contains("@"))
                .toList();

        Map<String, FolderNode> nodeMap = new HashMap<>();
        for (Archivos folderObj : carpetasFlat) {
            String path = folderObj.getUbicacion().replace("\\", "/").replaceAll("^/+|/+$", "").trim();
            nodeMap.put(path, new FolderNode(folderObj));
        }

        for (Archivos a : todos) {
            if (!a.isEsCarpeta()) {
                String path = a.getUbicacion().replace("\\", "/").replaceAll("^/+|/+$", "").trim();
                String fileName = a.getNombre();
                if (path.endsWith(fileName)) {
                    path = path.substring(0, path.length() - fileName.length()).replaceAll("^/+|/+$", "").trim();
                }
                if (nodeMap.containsKey(path)) {
                    nodeMap.get(path).addFileCount(1);
                }
            }
        }

        List<FolderNode> carpetasTree = new ArrayList<>();
        for (FolderNode node : nodeMap.values()) {
            String path = node.getFolder().getUbicacion().replace("\\", "/").replaceAll("^/+|/+$", "").trim();
            String folderName = node.getFolder().getNombre();
            String parentPath = "";
            if (path.endsWith(folderName)) {
                parentPath = path.substring(0, path.length() - folderName.length()).replaceAll("^/+|/+$", "").trim();
            }
            if (!parentPath.isEmpty() && nodeMap.containsKey(parentPath)) {
                nodeMap.get(parentPath).getChildren().add(node);
            } else {
                carpetasTree.add(node);
            }
        }

        // Sort children alphabetically recursively
        sortNodes(carpetasTree);

        model.addAttribute("usuarioActualObjeto", usuarioData);
        model.addAttribute("usuarioActual", loginId);
        model.addAttribute("carpetasTree", carpetasTree);
        model.addAttribute("archivos", archivosPagina);
        model.addAttribute("folderActual", folderActualURL);
        model.addAttribute("currentPage", archivosPage.getNumber());
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("pageSize", pageable.getPageSize());
        model.addAttribute("startItem", startItem);
        model.addAttribute("endItem", endItem);
        model.addAttribute("pageItems", getPageItems(archivosPage.getNumber(), totalPages));
        model.addAttribute("buscar", buscar);
        model.addAttribute("tipo", tipo);
        model.addAttribute("estado", estado);

        String totalTamanoFormateado;
        if (totalSizeBytes < 1024) {
            totalTamanoFormateado = totalSizeBytes + " B";
        } else if (totalSizeBytes < 1024 * 1024) {
            totalTamanoFormateado = String.format(java.util.Locale.US, "%.1f KB", (double) totalSizeBytes / 1024);
        } else if (totalSizeBytes < 1024 * 1024 * 1024) {
            totalTamanoFormateado = String.format(java.util.Locale.US, "%.1f MB", (double) totalSizeBytes / (1024 * 1024));
        } else {
            totalTamanoFormateado = String.format(java.util.Locale.US, "%.2f GB", (double) totalSizeBytes / (1024 * 1024 * 1024));
        }

        String totalDiscoFormateado;
        if (totalDiskBytes < 1024 * 1024 * 1024) {
            totalDiscoFormateado = String.format(java.util.Locale.US, "%.1f MB", (double) totalDiskBytes / (1024 * 1024));
        } else {
            totalDiscoFormateado = String.format(java.util.Locale.US, "%.1f GB", (double) totalDiskBytes / (1024 * 1024 * 1024));
        }

        long freeDiskBytes = superFolderFile.getUsableSpace();
        String espacioLibreFormateado;
        if (freeDiskBytes < 1024 * 1024 * 1024) {
            espacioLibreFormateado = String.format(java.util.Locale.US, "%.1f MB", (double) freeDiskBytes / (1024 * 1024));
        } else {
            espacioLibreFormateado = String.format(java.util.Locale.US, "%.1f GB", (double) freeDiskBytes / (1024 * 1024 * 1024));
        }

        model.addAttribute("totalCarpetas", foldersCount);
        model.addAttribute("totalArchivos", filesCount);
        model.addAttribute("totalSizeBytes", totalSizeBytes);
        model.addAttribute("totalTamanoFormateado", totalTamanoFormateado);
        model.addAttribute("totalDiscoFormateado", totalDiscoFormateado);
        model.addAttribute("espacioLibreFormateado", espacioLibreFormateado);
        model.addAttribute("totalGbUsado", String.format(java.util.Locale.US, "%.2f", gbUsed));
        model.addAttribute("porcentajeUsado", String.format(java.util.Locale.US, "%.1f", pctUsed));
        model.addAttribute("pendientesRevision", pendingCount);
        model.addAttribute("aprobadosCount", approvedCount);
        model.addAttribute("rechazadosCount", rejectedCount);
        model.addAttribute("requeridosCount", requeridosCount);
        model.addAttribute("opcionalesCount", opcionalesCount);
        model.addAttribute("compartidosCount", compartidosCount);
        model.addAttribute("categoriaActual", categoria != null ? categoria : "todos");
        model.addAttribute("tabActual", tab != null ? tab : "mis-docs");
        model.addAttribute("tipoActual", tipo != null ? tipo : "");
        model.addAttribute("estadoActual", estado != null ? estado : "");

        if (usuarioActual != null && "ROLE_CANDIDATO".equals(usuarioActual.getRol())) {
            return "candidato-documentos";
        }

        return "drive";
    }

    private void sortNodes(List<FolderNode> nodes) {
        nodes.sort((n1, n2) -> n1.getFolder().getNombre().compareToIgnoreCase(n2.getFolder().getNombre()));
        for (FolderNode node : nodes) {
            if (!node.getChildren().isEmpty()) {
                sortNodes(node.getChildren());
            }
        }
    }

    @PostMapping("/crear-carpeta")
    public String crearCarpeta(@RequestParam("nombre") String nombre,
            @RequestParam(value = "folder", defaultValue = "") String folder,
            Principal principal, Model model) {
        String loginId = (principal != null) ? principal.getName() : null;
        if (loginId == null)
            return "redirect:/login";

        Usuario usuarioActual = usuarioRepository.findByEmail(loginId).orElse(null);
        String email = (usuarioActual != null) ? usuarioActual.getEmail() : loginId;

        if (nombre == null || nombre.trim().isEmpty())
            return "redirect:/drive";

        folder = Sanitizer.sanitizePath(folder);
        String rutaSinSuper = Sanitizer.sanitizePath(nombre);
        if (rutaSinSuper.isEmpty())
            return "redirect:/drive";

        try {
            filesServices.crearCarpetaDrive(rutaSinSuper, folder, email);
        } catch (IOException e) {
            model.addAttribute("error", "No se pudo crear la carpeta: " + e.getMessage());
            return "redirect:/drive";
        }
        String rutaRelativa = folder.isEmpty() ? rutaSinSuper : folder + "/" + rutaSinSuper;
        return "redirect:/drive?folder=" + rutaRelativa;
    }

    @PostMapping("/renombrar-carpeta")
    public String renombrarCarpeta(@RequestParam("oldPath") String oldPath,
            @RequestParam("newName") String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            return "redirect:/drive";
        }
        try {
            filesServices.renombrarCarpeta(oldPath, Sanitizer.sanitizePath(newName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "redirect:/drive";
    }

    @PostMapping("/eliminar-carpeta")
    public String eliminarCarpeta(@RequestParam("folderPath") String folderPath, Principal principal) {
        String email = (principal != null) ? principal.getName() : null;
        if (email == null)
            return "redirect:/login";

        Usuario usuarioActual = usuarioRepository.findByEmail(email).orElse(null);
        if (usuarioActual == null)
            return "redirect:/drive";

        String normalized = folderPath.replace("\\", "/").replaceAll("^/+|/+$", "");
        Archivos carpetaDb = null;
        List<Archivos> carpetas = filesRepository.findFoldersByUbicacionStartingWith(normalized);
        for (Archivos a : carpetas) {
            String u = a.getUbicacion().replace("\\", "/").replaceAll("^/+|/+$", "");
            if (u.equals(normalized) || u.equals(normalized + "/")) {
                carpetaDb = a;
                break;
            }
        }

        if (!"ROLE_RRHH".equals(usuarioActual.getRol())) {
            boolean esPropietario = carpetaDb != null
                    && carpetaDb.getPropietario() != null
                    && email.equalsIgnoreCase(carpetaDb.getPropietario());
            if (!esPropietario) {
                return "redirect:/drive";
            }
        }

        try {
            filesServices.eliminarCarpetaRecursiva(folderPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "redirect:/drive";
    }

    @PostMapping("/subir-archivo")
    public String subirArchivo(@RequestParam("archivo") MultipartFile archivo,
            @RequestParam(value = "folder", defaultValue = "") String folder,
            @RequestParam(value = "candidatoId", required = false) Long candidatoId,
            @RequestParam(value = "fileId", required = false) Long fileId,
            Principal principal) {
        String loginId = (principal != null) ? principal.getName() : null;
        if (loginId == null)
            return "redirect:/login";

        Usuario usuarioActual = usuarioRepository.findByEmail(loginId).orElse(null);
        String email = (usuarioActual != null) ? usuarioActual.getEmail() : loginId;

        if (fileId != null) {
            Archivos existente = filesRepository.findById(fileId).orElse(null);
            if (existente != null) {
                try {
                    java.nio.file.Path rutaCompleta = java.nio.file.Paths.get(existente.getUbicacion());
                    java.nio.file.Files.createDirectories(rutaCompleta.getParent());
                    java.nio.file.Files.copy(archivo.getInputStream(), rutaCompleta, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    existente.setEstadoDocumento("Pendiente");
                    existente.setObservacion(null);
                    existente.setFechaSubida(LocalDateTime.now());
                    filesRepository.save(existente);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return "redirect:/drive?folder=" + folder;
            }
        }

        Usuario candidatoVinculado = null;
        if (usuarioActual != null && "ROLE_CANDIDATO".equals(usuarioActual.getRol())) {
            candidatoVinculado = usuarioActual;
        } else if (candidatoId != null) {
            candidatoVinculado = usuarioRepository.findById(candidatoId).orElse(null);
        }

        if (candidatoVinculado != null) {
            Candidato candidatoInfo = candidatoRepository.findById(candidatoVinculado.getId()).orElse(null);
            String nombreCandidato = candidatoInfo != null
                    ? (candidatoInfo.getUsername() + " "
                            + (candidatoInfo.getApellido() != null ? candidatoInfo.getApellido() : "")).trim()
                    : candidatoVinculado.getEmail();

            String candidateFolderPath = "Candidatos/" + nombreCandidato;
            filesServices.asegurarCarpetaCandidato(candidateFolderPath, nombreCandidato, email);
            folder = candidateFolderPath;
        }

        folder = Sanitizer.sanitizePath(folder);
        String filename = archivo.getOriginalFilename();
        if (!Sanitizer.isValidFileName(filename)) {
            return "redirect:/drive?folder=" + folder;
        }

        if (archivo.getSize() > 30L * 1024 * 1024) {
            return "redirect:/drive?folder=" + folder + "&error="
                    + URLEncoder.encode("El archivo supera el tamaño máximo permitido de 30 MB", StandardCharsets.UTF_8);
        }

        try {
            Archivos doc = filesServices.subirArchivoDrive(archivo, folder, email, filename, candidatoVinculado);
            if ("ROLE_RRHH".equals(usuarioActual.getRol())) {
                doc.setEstadoDocumento("No aplica");
                filesRepository.save(doc);
            }
        } catch (IOException e) {
            return "redirect:/drive?folder=" + folder;
        }

        return "redirect:/drive?folder=" + folder;
    }

    private boolean esPropietarioODestinatario(Archivos archivo, String email) {
        if (email == null)
            return false;
        Usuario usuarioActual = usuarioRepository.findByEmail(email).orElse(null);
        if (usuarioActual != null && ("ROLE_RRHH".equals(usuarioActual.getRol()) || "ROLE_ADMINISTRADOR".equals(usuarioActual.getRol()))) {
            return true;
        }
        String emailClean = email.trim();
        boolean propMatch = archivo.getPropietario() != null && emailClean.equalsIgnoreCase(archivo.getPropietario().trim());
        boolean destMatch = archivo.getDestinario() != null && emailClean.equalsIgnoreCase(archivo.getDestinario().trim());
        boolean candMatch = archivo.getCandidato() != null && archivo.getCandidato().getEmail() != null && emailClean.equalsIgnoreCase(archivo.getCandidato().getEmail().trim());
        return propMatch || destMatch || candMatch;
    }

    private String extraerCarpetaContenedora(String ubicacion) {
        if (ubicacion == null || ubicacion.isBlank()) {
            return null;
        }
        String path = ubicacion.replace("\\", "/").replace("superfolder/", "").replaceAll("^/+|/+$", "");
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash < 0) {
            return null;
        }
        String carpeta = path.substring(0, lastSlash);
        if (carpeta.isBlank()) {
            return null;
        }
        int parentSlash = carpeta.lastIndexOf('/');
        return parentSlash >= 0 ? carpeta.substring(parentSlash + 1) : carpeta;
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
            if (!resource.exists())
                return ResponseEntity.notFound().build();
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
        if (archivoOpt.isEmpty() || email == null)
            return "redirect:/drive?folder=" + folder;
        Archivos archivo = archivoOpt.get();
        if (!email.equalsIgnoreCase(archivo.getPropietario()))
            return "redirect:/drive?folder=" + folder;

        filesServices.eliminarArchivo(archivo);

        return "redirect:/drive?folder=" + folder;
    }

    @PostMapping("/compartir")
    public String compartirArchivo(@RequestParam("archivoId") Long archivoId,
            @RequestParam("emailDestinatario") String destinatario,
            Principal principal) {
        String email = principal != null ? principal.getName() : null;
        Optional<Archivos> archivoOpt = filesRepository.findById(archivoId);
        if (archivoOpt.isEmpty() || email == null)
            return "redirect:/drive";

        Usuario usuarioActual = usuarioRepository.findByEmail(email).orElse(null);
        boolean esRRHH = usuarioActual != null && ("ROLE_RRHH".equals(usuarioActual.getRol()) || "ROLE_ADMINISTRADOR".equals(usuarioActual.getRol()));
        boolean esPropietario = email.equalsIgnoreCase(archivoOpt.get().getPropietario());

        if (!esRRHH && !esPropietario)
            return "redirect:/drive";

        Archivos archivo = archivoOpt.get();
        String destClean = destinatario != null ? destinatario.trim() : "";
        filesServices.compartirArchivo(archivoId, destClean);

        if (!destClean.isBlank()) {
            Usuario destUser = usuarioRepository.findByEmail(destClean).orElse(null);
            if (destUser != null) {
                if (archivo.getCandidato() == null && "ROLE_CANDIDATO".equals(destUser.getRol())) {
                    archivo.setCandidato(destUser);
                    filesRepository.save(archivo);
                }
                Candidato cand = candidatoRepository.findById(destUser.getId()).orElse(null);
                String candNombre = cand != null ? (cand.getUsername() + " " + (cand.getApellido() != null ? cand.getApellido() : "")).trim() : destUser.getEmail();
                try {
                    notificacionService.crear("DOCUMENTO", "Se ha compartido contigo el archivo: " + archivo.getNombre(), destUser.getId(), candNombre, "/drive");
                } catch (Exception e) {
                    // Ignore notification error
                }
            }
        }

        return "redirect:/drive?compartido=ok";
    }

    @PostMapping("/actualizar-estado")
    public String actualizarEstado(@RequestParam("usuarioId") Long id,
            @RequestParam("nuevoEstado") String estado,
            Principal principal) {
        String email = principal != null ? principal.getName() : null;
        if (email == null) {
            return "redirect:/drive";
        }
        Usuario usuarioActual = usuarioRepository.findByEmail(email).orElse(null);
        if (usuarioActual == null || !"ROLE_RRHH".equals(usuarioActual.getRol())) {
            return "redirect:/drive";
        }

        Candidato candidato = candidatoRepository.findById(id).orElse(null);
        if (candidato == null)
            return "redirect:/drive";
        if (!CandidatoService.ESTADOS_VALIDOS.contains(estado))
            return "redirect:/drive";

        String estadoAnterior = candidato.getEstado();
        candidato.setEstado(estado);
        candidato.setUltimaActualizacion(LocalDateTime.now());
        candidatoRepository.save(candidato);

        if (estadoAnterior == null || !estadoAnterior.equals(estado)) {
            String responsable = principal != null ? principal.getName() : "RRHH";
            historialService.registrarCambio(id, estadoAnterior, estado, responsable);

            String nombre = candidato.getUsername() + " "
                    + (candidato.getApellido() != null ? candidato.getApellido() : "");
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
            if (!resource.exists())
                return ResponseEntity.notFound().build();
            String contentType = java.nio.file.Files.probeContentType(path);
            if (contentType == null)
                contentType = "application/octet-stream";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + archivo.getNombre() + "\"")
                    .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/actualizar-estado-archivo")
    public String actualizarEstadoArchivo(@RequestParam("archivoId") Long archivoId,
            @RequestParam("estado") String estado,
            @RequestParam(value = "observacion", required = false) String observacion,
            @RequestParam(value = "folder", defaultValue = "") String folder,
            Principal principal) {
        String email = principal != null ? principal.getName() : null;
        if (email == null)
            return "redirect:/drive?folder=" + folder;

        Usuario usuarioActual = usuarioRepository.findByEmail(email).orElse(null);
        if (usuarioActual == null || !"ROLE_RRHH".equals(usuarioActual.getRol())) {
            return "redirect:/drive?folder=" + folder;
        }

        Optional<Archivos> archivoOpt = filesRepository.findById(archivoId);
        boolean estadoValido = "Aprobado".equals(estado) || "Rechazado".equals(estado);
        boolean observacionObligatoria = "Rechazado".equals(estado) && (observacion == null || observacion.isBlank());
        if (!estadoValido || observacionObligatoria) {
            return "redirect:/drive?folder=" + folder + "&estadoError=" + ("Rechazado".equals(estado) ? "observacion" : "estado");
        }
        if (archivoOpt.isPresent()) {
            Archivos archivo = archivoOpt.get();
            boolean esPropietario = archivo.getPropietario() != null && email.equalsIgnoreCase(archivo.getPropietario());
            boolean noRevisable = "No aplica".equals(archivo.getEstadoDocumento());
            if (esPropietario || noRevisable) {
                return "redirect:/drive?folder=" + folder;
            }
            archivo.setEstadoDocumento(estado);
            if ("Rechazado".equals(estado)) {
                archivo.setObservacion(observacion);
            } else {
                archivo.setObservacion(null);
            }
            filesRepository.save(archivo);
        }

        return "redirect:/drive?folder=" + folder;
    }

    private List<PageItem> getPageItems(int current, int total) {
        List<PageItem> items = new ArrayList<>();
        if (total <= 5) {
            for (int i = 0; i < total; i++)
                items.add(new PageItem(i, false));
            return items;
        }
        items.add(new PageItem(0, false));
        if (current > 2)
            items.add(new PageItem(-1, true));
        int start = Math.max(1, current - 1);
        int end = Math.min(total - 2, current + 1);
        if (current <= 2)
            end = Math.min(3, total - 2);
        if (current >= total - 3)
            start = Math.max(total - 4, 1);
        for (int i = start; i <= end; i++)
            items.add(new PageItem(i, false));
        if (current < total - 3)
            items.add(new PageItem(-1, true));
        items.add(new PageItem(total - 1, false));
        return items;
    }

    public record PageItem(int number, boolean ellipsis) {
    }
}
