package com.back.controller;

import com.back.model.Usuario;
import com.back.model.Archivos;
import com.back.model.Evento;
import com.back.repository.UsuarioRepository;
import com.back.repository.ArchivosRepository;
import com.back.repository.EventoRepository;
import com.back.service.CandidatoService;
import com.back.service.CvService;
import com.back.service.EventoService;
import com.back.service.ExcelService;
import com.back.service.FilesServices;
import com.back.service.NotificacionService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/gestion-candidatos")
public class CandidatoController {

    @Autowired
    private CandidatoService candidatoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EventoService eventoService;

    @Autowired
    private ArchivosRepository archivosRepository;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private FilesServices filesServices;

    @Autowired
    private ExcelService excelService;

    @Autowired
    private CvService cvService;

    @Autowired
    private NotificacionService notificacionService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

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

        Page<Usuario> candidatos = candidatoService.listarCandidatos(
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
            "Disponible", "En proceso", "Entrevista", "Pendiente", "Contratado", "Descartado"
        ));

        List<Integer> expOptions = Arrays.asList(1, 2, 3, 5, 10);
        model.addAttribute("experienciaOptions", expOptions);

        model.addAttribute("selectedId", selectedId);

        return "gestion-candidatos";
    }

    @GetMapping("/detalle/{id}")
    public String verDetalle(@PathVariable Long id) {
        return "redirect:/gestion-candidatos?selectedId=" + id;
    }

    @GetMapping("/api")
    @ResponseBody
    public Map<String, Object> listarApi(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size,
                                          @RequestParam(required = false) String search,
                                          @RequestParam(required = false) String cargo,
                                          @RequestParam(required = false) String estado,
                                          @RequestParam(required = false) String experiencia,
                                          @RequestParam(required = false) String ciudad) {

        Page<Usuario> candidatos = candidatoService.listarCandidatos(
            search, cargo, estado, experiencia, ciudad, page, size);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("es"));

        List<Map<String, Object>> data = candidatos.getContent().stream().map(c -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", c.getId());
            m.put("nombre", c.getUsername());
            m.put("apellido", c.getApellido() != null ? c.getApellido() : "");
            m.put("ciudad", c.getCiudad() != null ? c.getCiudad() : "");
            m.put("cargo", c.getCargo() != null ? c.getCargo() : "");
            m.put("email", c.getEmail());
            m.put("telefono", c.getTelefono() != null ? c.getTelefono() : "");
            m.put("estado", c.getEstado() != null ? c.getEstado() : "Registrado");
            m.put("procesoActual", c.getProcesoActual() != null ? c.getProcesoActual() : "");
            m.put("ultimaActualizacion", c.getUltimaActualizacion() != null ? c.getUltimaActualizacion().format(fmt) : "");
            m.put("matchScore", candidatoService.calcularMatchScore(c));
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("data", data);
        result.put("currentPage", candidatos.getNumber());
        result.put("totalPages", candidatos.getTotalPages());
        result.put("totalElements", candidatos.getTotalElements());
        result.put("pageSize", candidatos.getSize());
        result.put("from", candidatos.getNumber() * candidatos.getSize() + 1);
        result.put("to", Math.min((candidatos.getNumber() + 1) * candidatos.getSize(), (int) candidatos.getTotalElements()));

        return result;
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> detalleCandidato(@PathVariable Long id) {
        Optional<Usuario> opt = usuarioRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Usuario c = opt.get();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("nombre", c.getUsername() + " " + (c.getApellido() != null ? c.getApellido() : ""));
        m.put("apellido", c.getApellido() != null ? c.getApellido() : "");
        m.put("email", c.getEmail());
        m.put("telefono", c.getTelefono() != null ? c.getTelefono() : "");
        m.put("cargo", c.getCargo() != null ? c.getCargo() : "");
        m.put("ciudad", c.getCiudad() != null ? c.getCiudad() : "");
        m.put("linkedin", c.getLinkedin() != null ? c.getLinkedin() : "");
        m.put("tecnologias", c.getTecnologias() != null ? c.getTecnologias() : "");
        m.put("idiomas", c.getIdiomas() != null ? c.getIdiomas() : "");
        m.put("experiencia", c.getExperiencia() != null ? c.getExperiencia() : 0);
        m.put("disponibilidad", c.getDisponibilidad() != null ? c.getDisponibilidad() : "");
        m.put("estado", c.getEstado() != null ? c.getEstado() : "Registrado");
        m.put("procesoActual", c.getProcesoActual() != null ? c.getProcesoActual() : "");
        m.put("fotoUrl", c.getFotoUrl() != null ? c.getFotoUrl() : "");
        m.put("matchScore", candidatoService.calcularMatchScore(c));
        m.put("matchLabel", candidatoService.getMatchLabel(candidatoService.calcularMatchScore(c)));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("es"));
        m.put("ultimaActualizacion", c.getUltimaActualizacion() != null ? c.getUltimaActualizacion().format(fmt) : "");

        return ResponseEntity.ok(m);
    }

    @PostMapping("/{id}/estado")
    @ResponseBody
    public ResponseEntity<?> actualizarEstado(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Optional<Usuario> opt = usuarioRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        String estado = body.getOrDefault("estado", body.getOrDefault("estado_nuevo", ""));
        if (estado.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Estado requerido"));
        }
        Usuario c = opt.get();
        String estadoAnterior = c.getEstado();
        c.setEstado(estado);
        c.setUltimaActualizacion(LocalDateTime.now());
        usuarioRepository.save(c);

        if (estadoAnterior == null || !estadoAnterior.equals(estado)) {
            String nombre = c.getUsername() + " " + (c.getApellido() != null ? c.getApellido() : "");
            notificacionService.crear("ESTADO",
                "Estado actualizado: " + nombre + " ahora como \"" + estado + "\"",
                id, nombre, "/gestion-candidatos");
        }

        return ResponseEntity.ok(Map.of("success", true, "estado", estado));
    }

    @PostMapping("/{id}/editar")
    @ResponseBody
    public ResponseEntity<?> editarCandidato(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Optional<Usuario> opt = usuarioRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Usuario c = opt.get();

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

        if (nombre.isBlank() || apellido.isBlank() || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Nombre, apellido y email son obligatorios"));
        }

        c.setUsername(nombre);
        c.setApellido(apellido);
        c.setEmail(email);
        c.setTelefono(telefono);
        c.setCargo(cargo);
        c.setCiudad(ciudad);
        try {
            c.setExperiencia(Integer.parseInt(experienciaStr));
        } catch (NumberFormatException e) {
            c.setExperiencia(0);
        }
        c.setDisponibilidad(disponibilidad);
        c.setTecnologias(tecnologias);
        c.setIdiomas(idiomas);
        c.setProcesoActual(procesoActual);
        c.setUltimaActualizacion(LocalDateTime.now());
        usuarioRepository.save(c);

        String nombreEdit = c.getUsername() + " " + (c.getApellido() != null ? c.getApellido() : "");
        notificacionService.crear("EDICION",
            "Perfil editado: " + nombreEdit,
            id, nombreEdit, "/gestion-candidatos");

        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/{id}/eliminar")
    @ResponseBody
    public ResponseEntity<?> eliminarCandidato(@PathVariable Long id) {
        Optional<Usuario> opt = usuarioRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Usuario c = opt.get();
        String email = c.getEmail();
        String prefix = "superfolder/Candidatos/" + email;
        List<Archivos> docs = archivosRepository.findByUbicacionStartingWith(prefix);
        for (Archivos doc : docs) {
            try {
                java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(doc.getUbicacion()));
            } catch (java.io.IOException ignored) {}
            archivosRepository.delete(doc);
        }
        eventoRepository.deleteByCandidatoId(id);
        usuarioRepository.delete(c);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/stats")
    @ResponseBody
    public Map<String, Object> stats() {
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("total", candidatoService.contarActivos());
        s.put("nuevos", candidatoService.contarNuevos());
        s.put("enProceso", candidatoService.contarEnProceso());
        s.put("contratados", candidatoService.contarContratables());
        return s;
    }

    @GetMapping("/{id}/documentos")
    @ResponseBody
    public ResponseEntity<?> documentosCandidato(@PathVariable Long id) {
        Optional<Usuario> opt = usuarioRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        String email = opt.get().getEmail();
        String prefix = "superfolder/Candidatos/" + email;
        List<Archivos> docs = archivosRepository.findByUbicacionStartingWith(prefix);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("es"));
        List<Map<String, Object>> list = docs.stream().map(d -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", d.getId());
            m.put("nombre", d.getNombre());
            m.put("tipoDocumento", d.getTipoDocumento() != null ? d.getTipoDocumento() : "Otro");
            m.put("etapa", d.getEtapa() != null ? d.getEtapa() : "");
            m.put("estado", d.getDestinario() != null && !d.getDestinario().isEmpty() ? "Compartido" : "Privado");
            m.put("ubicacion", d.getUbicacion());
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @PostMapping("/{id}/documentos/subir")
    @ResponseBody
    public ResponseEntity<?> subirDocumento(@PathVariable Long id,
                                             @RequestParam("file") MultipartFile file,
                                             @RequestParam(required = false) String tipo) {
        Optional<Usuario> opt = usuarioRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        String email = opt.get().getEmail();
        try {
            Archivos doc = filesServices.guardarDocumento(file, email, tipo);
            return ResponseEntity.ok(Map.of("success", true, "id", doc.getId(), "nombre", doc.getNombre()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/eventos")
    @ResponseBody
    public ResponseEntity<?> eventosCandidato(@PathVariable Long id) {
        Optional<Usuario> opt = usuarioRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        List<Evento> eventos = eventoRepository.findByCandidatoIdOrderByFechaDescHoraDesc(id);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("es"));
        List<Map<String, Object>> list = eventos.stream().map(e -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", e.getId());
            m.put("titulo", e.getTipo() != null ? e.getTipo() : "Entrevista");
            m.put("fecha", e.getFecha() != null ? e.getFecha().format(fmt) : "");
            m.put("hora", e.getHora() != null ? e.getHora().toString() : "");
            m.put("estado", e.getEstado() != null ? e.getEstado() : "");
            m.put("tipo", e.getTipo() != null ? e.getTipo() : "");
            m.put("lugar", e.getLugar() != null ? e.getLugar() : "");
            m.put("observaciones", e.getObservaciones() != null ? e.getObservaciones() : "");
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}/cv")
    public void descargarCV(@PathVariable Long id, HttpServletResponse response) throws IOException {
        Optional<Usuario> opt = usuarioRepository.findById(id);
        if (opt.isEmpty()) {
            response.sendRedirect("/gestion-candidatos");
            return;
        }
        cvService.generarCv(opt.get(), response);
    }

    @GetMapping("/export")
    public void exportarExcel(@RequestParam(required = false) String search,
                              @RequestParam(required = false) String estado,
                              HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=candidatos_reporte.xlsx");

        List<Usuario> candidatos = candidatoService.listarCandidatosSinPaginar(search, estado);
        excelService.exportarCandidatos(candidatos, response);
    }
}
