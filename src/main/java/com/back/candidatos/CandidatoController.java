package com.back.candidatos;

import com.back.auth.Usuario;
import com.back.auth.UsuarioService;
import com.back.drive.Archivos;
import com.back.drive.FilesServices;
import com.back.exportacion.CvService;
import com.back.exportacion.ExcelService;
import com.back.notificaciones.NotificacionService;
import com.back.shared.dto.CvDataDTO;
import com.back.shared.event.CandidatoEliminadoEvent;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/gestion-candidatos")
@RequiredArgsConstructor
public class CandidatoController {

    private final CandidatoService candidatoService;
    private final UsuarioService usuarioService;
    private final FilesServices filesServices;
    private final ApplicationEventPublisher eventPublisher;
    private final ExcelService excelService;
    private final CvService cvService;
    private final NotificacionService notificacionService;
    private final com.back.shared.HistorialService historialService;
    private final BCryptPasswordEncoder passwordEncoder;

    @GetMapping
    public String mostrarGestion(Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String cargo,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String experiencia,
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) Long selectedId) {

        Page<Candidato> candidatos = candidatoService.listarCandidatos(
                search, cargo, estado, experiencia, ciudad, page, size);

        model.addAttribute("activos", candidatoService.contarActivos());
        model.addAttribute("nuevos", candidatoService.contarNuevos());
        model.addAttribute("enProceso", candidatoService.contarEnProceso());
        model.addAttribute("contratables", candidatoService.contarContratables());

        model.addAttribute("candidatos", candidatos);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", candidatos.getTotalPages());
        model.addAttribute("totalElements", candidatos.getTotalElements());

        model.addAttribute("search", search);
        model.addAttribute("cargoFiltro", cargo);
        model.addAttribute("estadoFiltro", estado);
        model.addAttribute("experienciaFiltro", experiencia);
        model.addAttribute("ciudadFiltro", ciudad);

        model.addAttribute("cargos", candidatoService.getCargos());
        model.addAttribute("ciudades", candidatoService.getCiudades());

        model.addAttribute("estados", Arrays.asList(
                "Registrado", "En pruebas", "Entrevista", "Contratado", "No aceptado"));

        List<Integer> expOptions = Arrays.asList(1, 2, 3, 5, 10);
        model.addAttribute("experienciaOptions", expOptions);

        model.addAttribute("selectedId", selectedId);

        model.addAttribute("carpetas", Collections.emptyList());

        return "gestion-candidatos";
    }

    @GetMapping("/detalle/{id}")
    public String verDetalle(@PathVariable Long id) {
        return "redirect:/gestion-candidatos?selectedId=" + id;
    }

    @GetMapping("/api")
    @ResponseBody
    public CandidatoPageDTO listarApi(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String cargo,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String experiencia,
            @RequestParam(required = false) String ciudad) {

        Page<Candidato> candidatos = candidatoService.listarCandidatos(
                search, cargo, estado, experiencia, ciudad, page, size);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("es"));

        List<Long> ids = candidatos.getContent().stream().map(Candidato::getId).collect(Collectors.toList());
        Map<Long, Usuario> usuarioMap = usuarioService.mapearUsuariosPorIds(ids);

        List<CandidatoListaDTO> data = candidatos.getContent().stream().map(c -> {
            Usuario u = usuarioMap.get(c.getId());
            return new CandidatoListaDTO(
                    c.getId(),
                    c.getUsername(),
                    c.getApellido() != null ? c.getApellido() : "",
                    c.getCiudad() != null ? c.getCiudad() : "",
                    c.getCargo() != null ? c.getCargo() : "",
                    u != null ? u.getEmail() : "",
                    c.getTelefono() != null ? c.getTelefono() : "",
                    c.getEstado() != null ? c.getEstado() : "Registrado",
                    c.getProcesoActual() != null ? c.getProcesoActual() : "",
                    c.getUltimaActualizacion() != null ? c.getUltimaActualizacion().format(fmt) : "",
                    MatchScoreCalculator.calcularMatchScore(c));
        }).collect(Collectors.toList());

        return new CandidatoPageDTO(
                data,
                candidatos.getNumber(),
                candidatos.getTotalPages(),
                candidatos.getTotalElements(),
                candidatos.getSize(),
                candidatos.getNumber() * candidatos.getSize() + 1,
                Math.min((candidatos.getNumber() + 1) * candidatos.getSize(), (int) candidatos.getTotalElements()));
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> detalleCandidato(@PathVariable Long id) {
        Candidato candidato = candidatoService.buscarPorId(id).orElse(null);
        if (candidato == null) {
            return ResponseEntity.notFound().build();
        }
        Usuario usuario = usuarioService.buscarPorId(id).orElse(null);

        int score = MatchScoreCalculator.calcularMatchScore(candidato);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("es"));

        return ResponseEntity.ok(new DetalleCandidatoDTO(
                candidato.getId(),
                candidato.getUsername() + " " + (candidato.getApellido() != null ? candidato.getApellido() : ""),
                candidato.getApellido() != null ? candidato.getApellido() : "",
                usuario != null ? usuario.getEmail() : "",
                candidato.getTelefono() != null ? candidato.getTelefono() : "",
                candidato.getCargo() != null ? candidato.getCargo() : "",
                candidato.getCiudad() != null ? candidato.getCiudad() : "",
                candidato.getTecnologias() != null ? candidato.getTecnologias() : "",
                candidato.getIdiomas() != null ? candidato.getIdiomas() : "",
                candidato.getExperiencia() != null ? candidato.getExperiencia() : 0,
                candidato.getDisponibilidad() != null ? candidato.getDisponibilidad() : "",
                candidato.getEstado() != null ? candidato.getEstado() : "Registrado",
                candidato.getProcesoActual() != null ? candidato.getProcesoActual() : "",
                candidato.getFotoUrl() != null ? candidato.getFotoUrl() : "",
                score,
                MatchScoreCalculator.getMatchLabel(score),
                candidato.getUltimaActualizacion() != null ? candidato.getUltimaActualizacion().format(fmt) : ""));
    }

    @PostMapping("/{id}/estado")
    @ResponseBody
    public ResponseEntity<?> actualizarEstado(@PathVariable Long id, @RequestBody Map<String, String> body, java.security.Principal principal) {
        Candidato candidato = candidatoService.buscarPorId(id).orElse(null);
        if (candidato == null) {
            return ResponseEntity.notFound().build();
        }

        String estado = body.getOrDefault("estado", body.getOrDefault("estado_nuevo", ""));
        if (estado.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Estado requerido"));
        }
        if (!CandidatoService.ESTADOS_VALIDOS.contains(estado)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Estado inválido"));
        }
        String estadoAnterior = candidato.getEstado();
        candidato.setEstado(estado);
        candidato.setUltimaActualizacion(LocalDateTime.now());
        candidatoService.guardar(candidato);

        if (estadoAnterior == null || !estadoAnterior.equals(estado)) {
            String responsable = principal != null ? principal.getName() : "RRHH";
            historialService.registrarCambio(id, estadoAnterior, estado, responsable);

            String nombre = candidato.getUsername() + " "
                    + (candidato.getApellido() != null ? candidato.getApellido() : "");
            notificacionService.crear("ESTADO",
                    "Estado actualizado: " + nombre + " ahora como \"" + estado + "\"",
                    id, nombre, "/gestion-candidatos");
        }

        return ResponseEntity.ok(new EstadoResponseDTO(true, estado));
    }

    @GetMapping("/{id}/historial")
    @ResponseBody
    public ResponseEntity<List<com.back.shared.Historial>> obtenerHistorialCandidato(@PathVariable Long id) {
        return ResponseEntity.ok(historialService.obtenerHistorialPorCandidato(id));
    }

    @PostMapping("/{id}/editar")
    @ResponseBody
    public ResponseEntity<?> editarCandidato(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Candidato candidato = candidatoService.buscarPorId(id).orElse(null);
        if (candidato == null) {
            return ResponseEntity.notFound().build();
        }
        Usuario usuario = usuarioService.buscarPorId(id).orElse(null);

        String nombre = body.getOrDefault("nombre", "").trim();
        String apellido = body.getOrDefault("apellido", "").trim();
        String email = body.getOrDefault("email", "").trim();
        String telefono = body.getOrDefault("telefono", "").trim();
        String cargo = body.getOrDefault("cargo", "").trim();
        String ciudad = body.getOrDefault("ciudad", "").trim();
        String experienciaStr = body.getOrDefault("experiencia", "0").trim();
        String disponibilidad = body.getOrDefault("disponibilidad", "").trim();
        String tecnologias = body.getOrDefault("tecnologias", "").trim();
        String idiomas = body.getOrDefault("idiomas", "").trim();
        String procesoActual = body.getOrDefault("procesoActual", "").trim();

        if (nombre.isBlank() || !nombre.matches("^[A-Za-záéíóúÁÉÍÓÚñÑ\\s]{2,50}$")) {
            return ResponseEntity.badRequest().body(Map.of("error", "El nombre es obligatorio y debe contener solo letras (de 2 a 50 caracteres)"));
        }
        if (apellido.isBlank() || !apellido.matches("^[A-Za-záéíóúÁÉÍÓÚñÑ\\s]{2,50}$")) {
            return ResponseEntity.badRequest().body(Map.of("error", "El apellido es obligatorio y debe contener solo letras (de 2 a 50 caracteres)"));
        }
        if (email.isBlank() || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Ingresa un correo electrónico válido"));
        }
        if (!telefono.isBlank() && !telefono.matches("^[0-9]{10}$")) {
            return ResponseEntity.badRequest().body(Map.of("error", "El teléfono debe contener exactamente 10 dígitos numéricos"));
        }
        if (!cargo.isBlank() && !cargo.matches("^[A-Za-záéíóúÁÉÍÓÚñÑ\\s]{2,100}$")) {
            return ResponseEntity.badRequest().body(Map.of("error", "El cargo debe contener solo letras y espacios (de 2 a 100 caracteres)"));
        }
        if (!ciudad.isBlank() && !ciudad.matches("^[A-Za-záéíóúÁÉÍÓÚñÑ\\s]{2,50}$")) {
            return ResponseEntity.badRequest().body(Map.of("error", "La ciudad debe contener solo letras y espacios (de 2 a 50 caracteres)"));
        }

        if (usuario != null && !email.equalsIgnoreCase(usuario.getEmail())) {
            boolean emailEnUso = usuarioService.buscarPorEmail(email)
                    .map(u -> !u.getId().equals(id))
                    .orElse(false);
            if (emailEnUso) {
                return ResponseEntity.status(409).body(Map.of("error", "El correo electrónico ya se encuentra registrado por otro usuario"));
            }
        }

        candidato.setUsername(nombre);
        candidato.setApellido(apellido);
        candidato.setTelefono(telefono);
        candidato.setCargo(cargo);
        candidato.setCiudad(ciudad);
        try {
            int exp = Integer.parseInt(experienciaStr);
            candidato.setExperiencia(Math.max(0, Math.min(exp, 50)));
        } catch (NumberFormatException e) {
            candidato.setExperiencia(0);
        }
        candidato.setDisponibilidad(disponibilidad);
        candidato.setTecnologias(tecnologias.length() > 255 ? tecnologias.substring(0, 255) : tecnologias);
        candidato.setIdiomas(idiomas.length() > 255 ? idiomas.substring(0, 255) : idiomas);
        candidato.setProcesoActual(procesoActual);
        candidato.setUltimaActualizacion(LocalDateTime.now());
        candidatoService.guardar(candidato);

        if (usuario != null && !email.equals(usuario.getEmail())) {
            usuario.setEmail(email);
            usuarioService.guardar(usuario);
        }

        String nombreEdit = candidato.getUsername() + " "
                + (candidato.getApellido() != null ? candidato.getApellido() : "");
        notificacionService.crear("EDICION",
                "Perfil editado: " + nombreEdit,
                id, nombreEdit, "/gestion-candidatos");

        return ResponseEntity.ok(new EditarResponseDTO(true));
    }

    @PostMapping("/{id}/eliminar")
    @ResponseBody
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> eliminarCandidato(@PathVariable Long id) {
        Candidato candidato = candidatoService.buscarPorId(id).orElse(null);
        if (candidato == null) {
            return ResponseEntity.notFound().build();
        }
        Usuario usuario = usuarioService.buscarPorId(id).orElse(null);
        if (usuario != null) {
            String email = usuario.getEmail();
            List<Archivos> archivosAsociados = filesServices.buscarPorCandidatoIdOEmail(id, email);
            for (Archivos doc : archivosAsociados) {
                try {
                    if (doc.getUbicacion() != null) {
                        java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(doc.getUbicacion()));
                    }
                } catch (java.io.IOException ignored) {
                }
                filesServices.eliminar(doc);
            }

            String nombreCompleto = (candidato.getUsername() + " " + (candidato.getApellido() != null ? candidato.getApellido() : "")).trim();
            List<String> prefijos = List.of(
                    "superfolder/Candidatos/" + email,
                    "superfolder/Candidatos/" + nombreCompleto);
            for (String prefix : prefijos) {
                List<Archivos> docs = filesServices.buscarPorUbicacionPrefijo(prefix);
                for (Archivos doc : docs) {
                    try {
                        if (doc.getUbicacion() != null) {
                            java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(doc.getUbicacion()));
                        }
                    } catch (java.io.IOException ignored) {
                    }
                    filesServices.eliminar(doc);
                }
            }
        }
        notificacionService.eliminarPorCandidato(id);
        eventPublisher.publishEvent(new CandidatoEliminadoEvent(id));
        candidatoService.eliminar(candidato);
        if (usuario != null) {
            usuarioService.eliminar(usuario);
        }
        return ResponseEntity.ok(new EliminarResponseDTO(true));
    }

    @GetMapping("/stats")
    @ResponseBody
    public Map<String, Object> stats() {
        return Map.of(
                "total", candidatoService.contarActivos(),
                "documentos", filesServices.contarDocumentos(),
                "carpetas", filesServices.contarCarpetas(),
                "entrevistas", 0
        );
    }

    @GetMapping("/{id}/documentos")
    @ResponseBody
    public ResponseEntity<?> documentosCandidato(@PathVariable Long id) {
        Usuario usuario = usuarioService.buscarPorId(id).orElse(null);
        if (usuario == null)
            return ResponseEntity.notFound().build();
        String email = usuario.getEmail();
        String prefix = "superfolder/Candidatos/" + email;
        List<Archivos> docs = filesServices.buscarPorUbicacionPrefijo(prefix);
        List<DocumentoDTO> list = docs.stream().map(d -> new DocumentoDTO(
                d.getId(),
                d.getNombre(),
                d.getTipoDocumento() != null ? d.getTipoDocumento() : "Otro",
                d.getEtapa() != null ? d.getEtapa() : "",
                d.getDestinario() != null && !d.getDestinario().isEmpty() ? "Compartido" : "Privado",
                d.getUbicacion())).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @PostMapping("/{id}/documentos/subir")
    @ResponseBody
    public ResponseEntity<?> subirDocumento(@PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String tipo) {
        Usuario usuario = usuarioService.buscarPorId(id).orElse(null);
        if (usuario == null)
            return ResponseEntity.notFound().build();
        String email = usuario.getEmail();
        try {
            Archivos doc = filesServices.guardarDocumento(file, email, tipo);
            return ResponseEntity.ok(Map.of("success", true, "id", doc.getId(), "nombre", doc.getNombre()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }


    @GetMapping("/{id}/cv")
    public void descargarCV(@PathVariable Long id, HttpServletResponse response) throws IOException {
        Candidato candidato = candidatoService.buscarPorId(id).orElse(null);
        if (candidato == null) {
            response.sendRedirect("/gestion-candidatos");
            return;
        }
        Usuario usuario = usuarioService.buscarPorId(id).orElse(null);
        String email = usuario != null ? usuario.getEmail() : "";
        CvDataDTO cvData = new CvDataDTO(
            candidato.getUsername(),
            candidato.getApellido(),
            email,
            candidato.getTelefono(),
            candidato.getCiudad(),
            candidato.getCargo(),
            candidato.getExperiencia() != null ? candidato.getExperiencia() : 0,
            candidato.getTecnologias(),
            candidato.getIdiomas(),
            candidato.getDisponibilidad()
        );
        cvService.generarCv(cvData, response);
    }

    @GetMapping("/export")
    public void exportarExcel(@RequestParam(required = false) String search,
            @RequestParam(required = false) String estado,
            HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=candidatos_reporte.xlsx");

        List<Candidato> candidatos = candidatoService.listarCandidatosSinPaginar(search, estado);
        Map<Long, String> emails = usuarioService.buscarTodosPorIds(candidatos.stream().map(Candidato::getId).toList())
                .stream().collect(Collectors.toMap(Usuario::getId, Usuario::getEmail));
        String[] cabeceras = {"ID", "Nombre", "Apellido", "Email", "Tel\u00e9fono", "Cargo", "Ciudad", "Experiencia", "Disponibilidad", "Tecnolog\u00edas", "Idiomas", "Estado", "Proceso"};
        List<Object[]> datos = candidatos.stream()
            .map(c -> new Object[]{
                c.getId(),
                c.getUsername() != null ? c.getUsername() : "",
                c.getApellido() != null ? c.getApellido() : "",
                emails.getOrDefault(c.getId(), ""),
                c.getTelefono() != null ? c.getTelefono() : "",
                c.getCargo() != null ? c.getCargo() : "",
                c.getCiudad() != null ? c.getCiudad() : "",
                c.getExperiencia() != null ? c.getExperiencia() : 0,
                c.getDisponibilidad() != null ? c.getDisponibilidad() : "",
                c.getTecnologias() != null ? c.getTecnologias() : "",
                c.getIdiomas() != null ? c.getIdiomas() : "",
                c.getEstado() != null ? c.getEstado() : "Registrado",
                c.getProcesoActual() != null ? c.getProcesoActual() : ""
            }).toList();
        excelService.exportarDatos("Candidatos", cabeceras, datos, response);
    }
}
