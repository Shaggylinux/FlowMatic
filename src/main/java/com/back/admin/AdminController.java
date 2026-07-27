package com.back.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import com.back.admin.dto.ActividadRecienteDTO;
import com.back.admin.dto.UsuarioResumenDTO;
import com.back.auth.Usuario;
import com.back.auth.UsuarioRepository;
import com.back.auth.UsuarioService;
import com.back.candidatos.Candidato;
import com.back.candidatos.CandidatoRepository;
import com.back.calendario.EventoRepository;
import com.back.shared.ExcelService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UsuarioRepository usuarioRepository;
    private final CandidatoRepository candidatoRepository;
    private final RRHHRepository rrhhRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UsuarioService usuarioService;
    private final ExcelService excelService;
    private final EventoRepository eventoRepository;
    private final AuditoriaService auditoriaService;
    private final ConfiguracionService configuracionService;
    private final AdminService adminService;

    @GetMapping("/dashboard")
    public String dashboard(Model model,
            @RequestParam(name = "clave_ok", required = false) String claveOk,
            @RequestParam(name = "clave_error", required = false) String claveError,
            @RequestParam(name = "config_ok", required = false) String configOk) {
        long totalUsuarios = usuarioRepository.count();
        long totalRRHH = usuarioRepository.countByRol("ROLE_RRHH");
        long totalActivos = usuarioRepository.countByRolAndActivo("ROLE_RRHH", true);
        long totalPendientes = usuarioRepository.countByRolAndActivo("ROLE_RRHH", false);
        long totalBloqueados = 0;
        long totalCandidatos = usuarioRepository.countByRol("ROLE_CANDIDATO");
        long totalAdmins = usuarioRepository.countByRol("ROLE_ADMINISTRADOR");
        long entrevistasHoy = eventoRepository.countByFecha(LocalDate.now());

        List<Map<String, Object>> actividadReciente = buildActividadReciente();
        List<Map<String, Object>> distribucionRoles = buildDistribucionRoles(totalAdmins, totalRRHH, totalCandidatos);

        long sumaRRHH = totalActivos + totalPendientes;
        String rrhhDiff = totalUsuarios > 0
            ? "+" + (totalRRHH * 100 / totalUsuarios) + "% este mes" : "0%";
        String activosDiff = sumaRRHH > 0
            ? "+" + (totalActivos * 100 / sumaRRHH) + "% activos" : "0%";
        String pendientesDiff = totalPendientes > 0
            ? totalPendientes + " pendientes" : "0 pendientes";
        String bloqueadosDiff = "0 bloqueados";

        model.addAttribute("totalUsuarios", totalUsuarios);
        model.addAttribute("totalRRHH", totalRRHH);
        model.addAttribute("totalActivos", totalActivos);
        model.addAttribute("totalPendientes", totalPendientes);
        model.addAttribute("totalBloqueados", totalBloqueados);
        model.addAttribute("totalCandidatos", totalCandidatos);
        model.addAttribute("totalAdmins", totalAdmins);
        model.addAttribute("entrevistasHoy", entrevistasHoy);
        model.addAttribute("rrhhDiff", rrhhDiff);
        model.addAttribute("activosDiff", activosDiff);
        model.addAttribute("pendientesDiff", pendientesDiff);
        model.addAttribute("bloqueadosDiff", bloqueadosDiff);
        model.addAttribute("actividadReciente", actividadReciente);
        model.addAttribute("distribucionRoles", distribucionRoles);
        model.addAttribute("viewMode", "dashboard");
        model.addAttribute("claveOk", claveOk != null);
        model.addAttribute("claveError", claveError != null);
        model.addAttribute("adminEmail",
            SecurityContextHolder.getContext().getAuthentication().getName());
        model.addAttribute("configOk", configOk != null);

        return "admin";
    }

    private List<Map<String, Object>> buildDistribucionRoles(long admins, long rrhh, long candidatos) {
        long total = admins + rrhh + candidatos;
        List<Map<String, Object>> lista = new ArrayList<>();
        if (total == 0) return lista;

        double circumference = 314.16;
        double accumulated = 0;

        String[][] roles = {
            { "Administradores", String.valueOf(admins), "#0D9488" },
            { "RRHH",            String.valueOf(rrhh),   "#16A34A" },
            { "Candidatos",      String.valueOf(candidatos), "#0EA5E9" }
        };
        for (String[] r : roles) {
            long count = Long.parseLong(r[1]);
            if (count == 0) continue;
            Map<String, Object> m = new HashMap<>();
            m.put("label", r[0]);
            m.put("count", count);
            m.put("color", r[2]);
            m.put("pct", Math.round((double) count / total * 100));
            m.put("offset", Math.round(accumulated * 10.0) / 10.0);
            m.put("dashArray", Math.round((double) count / total * circumference * 10.0) / 10.0);
            accumulated += (double) count / total * circumference;
            lista.add(m);
        }
        return lista;
    }

    private List<Map<String, Object>> buildActividadReciente() {
        List<Auditoria> recientes = auditoriaService.obtenerRecientes(5);
        List<Map<String, Object>> actividades = new ArrayList<>();
        String[] colores = { "#0D9488", "#0EA5E9", "#8B5CF6", "#F59E0B", "#EF4444" };
        for (int i = 0; i < recientes.size(); i++) {
            Auditoria a = recientes.get(i);
            Map<String, Object> act = new HashMap<>();
            act.put("badge", a.getAccion());
            act.put("titulo", a.getDescripcion());
            act.put("usuario", a.getRealizadoPor() != null ? a.getRealizadoPor() : "Sistema");
            act.put("fecha", formatearFecha(a.getFecha()));
            act.put("tipo", a.getTipo());
            act.put("iniciales", obtenerInicialesDesdeNombre(a.getRealizadoPor()));
            act.put("colorAvatar", colores[i % colores.length]);
            actividades.add(act);
        }
        return actividades;
    }

    private String formatearFecha(LocalDateTime fecha) {
        long minutos = ChronoUnit.MINUTES.between(fecha, LocalDateTime.now());
        if (minutos < 1) return "Ahora";
        if (minutos < 60) return "Hace " + minutos + " minuto(s)";
        long horas = ChronoUnit.HOURS.between(fecha, LocalDateTime.now());
        if (horas < 24) return "Hace " + horas + " hora(s)";
        long dias = ChronoUnit.DAYS.between(fecha, LocalDateTime.now());
        if (dias < 7) return "Hace " + dias + " d\u00eda(s)";
        return java.time.format.DateTimeFormatter.ofPattern("d MMM, HH:mm").format(fecha);
    }

    private String obtenerInicialesDesdeNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) return "S";
        String[] partes = nombre.trim().split("\\s+");
        if (partes.length >= 2) {
            return (partes[0].charAt(0) + "" + partes[1].charAt(0)).toUpperCase();
        }
        return partes[0].substring(0, Math.min(2, partes[0].length())).toUpperCase();
    }
    @GetMapping
    public String panelAdmin(Model model,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "buscar", required = false) String buscar,
            @RequestParam(name = "rol", required = false) String rol) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Usuario> usuariosPage;

        boolean hasBuscar = buscar != null && !buscar.trim().isEmpty();
        boolean hasRol = rol != null && !rol.trim().isEmpty();

        if (hasBuscar && hasRol) {
            usuariosPage = usuarioRepository.findByRolAndEmailContainingIgnoreCase(rol, buscar.trim(), pageable);
        } else if (hasBuscar) {
            usuariosPage = usuarioRepository.findByEmailContainingIgnoreCase(buscar.trim(), pageable);
        } else if (hasRol) {
            usuariosPage = usuarioRepository.findByRol(rol, pageable);
        } else {
            usuariosPage = usuarioRepository.findAll(pageable);
        }

        long totalItems = usuariosPage.getTotalElements();
        int totalPages = usuariosPage.getTotalPages();
        int startItem = totalItems == 0 ? 0 : page * size + 1;
        int endItem = (int) Math.min((long) page * size + usuariosPage.getNumberOfElements(), totalItems);

        long totalUsuarios = usuarioRepository.count();
        long totalRRHH = usuarioRepository.countByRol("ROLE_RRHH");
        long totalCandidatos = usuarioRepository.countByRol("ROLE_CANDIDATO");
        long totalAdmins = usuarioRepository.countByRol("ROLE_ADMINISTRADOR");

        List<UsuarioResumenDTO> usuariosData = usuariosPage.getContent().stream()
                .map(adminService::mapToUsuarioResumen)
                .collect(Collectors.toList());

        model.addAttribute("usuarios", usuariosData);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("pageSize", usuariosPage.getSize());
        model.addAttribute("startItem", startItem);
        model.addAttribute("endItem", endItem);
        model.addAttribute("pageItems", getPageItems(page, totalPages));
        model.addAttribute("totalUsuarios", totalUsuarios);
        model.addAttribute("totalRRHH", totalRRHH);
        model.addAttribute("totalCandidatos", totalCandidatos);
        model.addAttribute("totalAdmins", totalAdmins);
        model.addAttribute("viewMode", "usuarios");
        model.addAttribute("buscar", buscar);
        model.addAttribute("rol", rol);

        return "admin";
    }

    private UsuarioResumenDTO mapToUsuarioResumen(Usuario u) {
        String nombre = obtenerNombreUsuario(u.getId(), u.getRol());
        String[] parts = nombre.split(" ", 2);
        return new UsuarioResumenDTO(
            u.getId(),
            parts.length > 0 ? parts[0] : "",
            parts.length > 1 ? parts[1] : "",
            u.getEmail(),
            u.getRol(),
            u.isActivo(),
            "Reci\u00e9n"
        );
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

    @PostMapping("/crear-rrhh")
    public String crearRRHH(@ModelAttribute Usuario nuevoRRHH,
                            @RequestParam String username,
                            @RequestParam String apellido,
                            @RequestParam(required = false) String telefono) {
        nuevoRRHH.setRol("ROLE_RRHH");

        String respuesta = usuarioService.registrarUsuario(nuevoRRHH, username, apellido, telefono);

        if ("DUPLICADO".equals(respuesta)) {
            return "redirect:/admin?error=duplicado";
        }

        if ("CLAVE_CORTA".equals(respuesta)) {
            return "redirect:/admin?error=clave_corta";
        }

        auditoriaService.registrar("CREACI\u00d3N",
            "Se cre\u00f3 el usuario RRHH " + username + " " + apellido,
            "Administrador", "USUARIO");
        return "redirect:/admin?pendiente";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id) {
        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        if (usuario != null) {
            String nombre = obtenerNombreUsuario(usuario.getId(), usuario.getRol());
            String email = usuario.getEmail();
            if ("ROLE_CANDIDATO".equals(usuario.getRol())) {
                candidatoRepository.deleteById(id);
            } else if ("ROLE_RRHH".equals(usuario.getRol())) {
                rrhhRepository.deleteById(id);
            }
            usuarioRepository.delete(usuario);

            auditoriaService.registrar("ELIMINACI\u00d3N",
                "Se elimin\u00f3 el usuario " + nombre + " (" + email + ")",
                "Administrador", "SEGURIDAD");
        }
        return "redirect:/admin";
    }

    @PostMapping("/editar")
    public String editarUsuario(@ModelAttribute Usuario datosEditados,
            @RequestParam(value = "nuevaClave", required = false) String nuevaClave,
            @RequestParam String username,
            @RequestParam String apellido) {

        Usuario usuarioBD = usuarioRepository.findById(datosEditados.getId()).orElse(null);

        if (usuarioBD != null) {
            String emailAnterior = usuarioBD.getEmail();
            usuarioBD.setEmail(datosEditados.getEmail());

            if (nuevaClave != null && !nuevaClave.trim().isEmpty()) {
                String claveEncriptada = passwordEncoder.encode(nuevaClave);
                usuarioBD.setClave(claveEncriptada);
            }
            usuarioRepository.save(usuarioBD);

            if ("ROLE_RRHH".equals(usuarioBD.getRol())) {
                RRHH rrhh = rrhhRepository.findById(usuarioBD.getId()).orElse(null);
                if (rrhh != null) {
                    rrhh.setUsername(username);
                    rrhh.setApellido(apellido);
                    rrhhRepository.save(rrhh);
                }
            }

            String nombreEditado = obtenerNombreUsuario(usuarioBD.getId(), usuarioBD.getRol());
            String cambios = !emailAnterior.equals(datosEditados.getEmail())
                ? " (email: " + emailAnterior + " \u2192 " + datosEditados.getEmail() + ")"
                : "";
            auditoriaService.registrar("EDICI\u00d3N",
                "Se edit\u00f3 el perfil de " + nombreEditado + cambios,
                "Administrador", "USUARIO");
        }
        return "redirect:/admin?editado";
    }

    @GetMapping("/exportar")
    public void exportarAExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=usuarios_reporte.xlsx";
        response.setHeader(headerKey, headerValue);

        List<Usuario> listaUsuarios = usuarioRepository.findAll();
        excelService.exportarUsuarios(listaUsuarios, response);

        auditoriaService.registrar("EXPORTACI\u00d3N",
            "Se export\u00f3 la lista de usuarios a Excel", "Administrador", "SISTEMA");
    }

    @GetMapping("/reportes/exportar")
    public void exportarReporte(HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=reporte_sistema.xlsx";
        response.setHeader(headerKey, headerValue);

        Map<String, Object> metricas = new HashMap<>();
        metricas.put("totalUsuarios", usuarioRepository.count());
        metricas.put("totalRRHH", usuarioRepository.countByRol("ROLE_RRHH"));
        metricas.put("totalActivos", usuarioRepository.countByRolAndActivo("ROLE_RRHH", true));
        metricas.put("totalPendientes", usuarioRepository.countByRolAndActivo("ROLE_RRHH", false));
        metricas.put("totalCandidatos", usuarioRepository.countByRol("ROLE_CANDIDATO"));
        metricas.put("totalAdmins", usuarioRepository.countByRol("ROLE_ADMINISTRADOR"));

        excelService.exportarReporte(metricas, response);

        auditoriaService.registrar("EXPORTACI\u00d3N",
            "Se export\u00f3 el reporte del sistema", "Administrador", "SISTEMA");
    }

    @GetMapping("/actividad")
    @ResponseBody
    public Map<String, Object> obtenerActividad(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        org.springframework.data.domain.Page<Auditoria> pagina = auditoriaService.obtenerPaginado(page, size);
        List<Map<String, Object>> items = new ArrayList<>();
        for (Auditoria a : pagina.getContent()) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", a.getId());
            m.put("accion", a.getAccion());
            m.put("descripcion", a.getDescripcion());
            m.put("realizadoPor", a.getRealizadoPor());
            m.put("tipo", a.getTipo());
            m.put("fecha", a.getFecha() != null ? a.getFecha().toString() : null);
            items.add(m);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("actividades", items);
        result.put("total", pagina.getTotalElements());
        result.put("page", pagina.getNumber());
        result.put("size", pagina.getSize());
        result.put("totalPages", pagina.getTotalPages());
        return result;
    }

    @GetMapping("/auditoria")
    @ResponseBody
    public Map<String, Object> obtenerAuditoria(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        org.springframework.data.domain.Page<Auditoria> pagina = auditoriaService.obtenerPorTipo("SEGURIDAD", page, size);
        long totalSeguridad = auditoriaService.contarPorTipo("SEGURIDAD");
        List<Map<String, Object>> items = new ArrayList<>();
        for (Auditoria a : pagina.getContent()) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", a.getId());
            m.put("accion", a.getAccion());
            m.put("descripcion", a.getDescripcion());
            m.put("realizadoPor", a.getRealizadoPor());
            m.put("fecha", a.getFecha() != null ? a.getFecha().toString() : null);
            items.add(m);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("eventos", items);
        result.put("total", pagina.getTotalElements());
        result.put("totalSeguridad", totalSeguridad);
        result.put("page", pagina.getNumber());
        result.put("size", pagina.getSize());
        result.put("totalPages", pagina.getTotalPages());
        return result;
    }

    @GetMapping("/configuraciones")
    @ResponseBody
    public List<Map<String, String>> obtenerConfiguraciones() {
        List<Configuracion> configs = configuracionService.obtenerTodas();

        Map<String, String> defaults = new HashMap<>();
        defaults.put("password.min.length", "8");
        defaults.put("login.max.attempts", "5");
        defaults.put("login.block.minutes", "15");
        defaults.put("app.name", "Flowmatic");
        defaults.put("app.support.email", "malacruz132@gmail.com");
        defaults.put("password.reset.expiry.minutes", "15");

        List<Map<String, String>> result = new ArrayList<>();
        for (var entry : defaults.entrySet()) {
            Map<String, String> m = new HashMap<>();
            m.put("clave", entry.getKey());
            m.put("valor", configuracionService.getValor(entry.getKey(), entry.getValue()));
            m.put("defecto", entry.getValue());
            result.add(m);
        }
        return result;
    }

    @PostMapping("/configuraciones")
    public String guardarConfiguraciones(@RequestParam Map<String, String> todas) {
        Map<String, String> configs = new HashMap<>();
        for (var entry : todas.entrySet()) {
            if (entry.getKey().startsWith("cfg_")) {
                String clave = entry.getKey().substring(4);
                configs.put(clave, entry.getValue());
            }
        }
        configuracionService.guardarTodas(configs);
        auditoriaService.registrar("EDICI\u00d3N",
            "Se actualizaron las configuraciones del sistema", "Administrador", "SISTEMA");
        return "redirect:/admin/dashboard?config_ok";
    }

    @PostMapping("/cambiar-clave")
    public String cambiarClave(@RequestParam String claveActual,
                               @RequestParam String nuevaClave,
                               @RequestParam String confirmarClave) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);

        if (usuario == null) {
            return "redirect:/admin/dashboard?clave_error";
        }

        if (!passwordEncoder.matches(claveActual, usuario.getClave())) {
            return "redirect:/admin/dashboard?clave_error";
        }

        if (!nuevaClave.equals(confirmarClave)) {
            return "redirect:/admin/dashboard?clave_error";
        }

        int minLength = Integer.parseInt(configuracionService.getValor("password.min.length", "8"));
        if (nuevaClave.trim().length() < minLength) {
            return "redirect:/admin/dashboard?clave_error";
        }

        usuario.setClave(passwordEncoder.encode(nuevaClave));
        usuarioRepository.save(usuario);

        auditoriaService.registrar("EDICI\u00d3N",
            "El administrador cambi\u00f3 su contrase\u00f1a", email, "SEGURIDAD");

        return "redirect:/admin/dashboard?clave_ok";
    }

    private String obtenerNombreUsuario(Long userId, String rol) {
        if (rol == null) return "Usuario";
        return switch (rol) {
            case "ROLE_CANDIDATO" -> candidatoRepository.findById(userId)
                .map(c -> c.getUsername() + " " + c.getApellido()).orElse("Candidato");
            case "ROLE_RRHH" -> rrhhRepository.findById(userId)
                .map(r -> r.getUsername() + " " + r.getApellido()).orElse("RRHH");
            case "ROLE_ADMINISTRADOR" -> "Administrador";
            default -> "Usuario";
        };
    }
}
