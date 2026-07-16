package com.back.controller;

import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.ui.Model;
import com.back.model.Usuario;
import com.back.model.Candidato;
import com.back.model.RRHH;
import com.back.repository.UsuarioRepository;
import com.back.repository.CandidatoRepository;
import com.back.repository.RRHHRepository;
import com.back.service.ExcelService;
import com.back.service.UsuarioService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CandidatoRepository candidatoRepository;

    @Autowired
    private RRHHRepository rrhhRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ExcelService excelService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        long totalUsuarios = usuarioRepository.count();
        long totalActivos = usuarioRepository.countByActivoTrue();
        long totalPendientes = usuarioRepository.countByActivoFalse();
        long totalRRHH = usuarioRepository.countByRol("ROLE_RRHH");
        long totalCandidatos = usuarioRepository.countByRol("ROLE_CANDIDATO");
        long totalAdmins = usuarioRepository.countByRol("ROLE_ADMINISTRADOR");

        List<Usuario> ultimosUsuarios = usuarioRepository.findTop10ByOrderByIdDesc();
        List<Map<String, Object>> actividadReciente = buildActividadReciente(ultimosUsuarios);
        List<Map<String, Object>> ultimosUsuariosData = buildUltimosUsuariosData(ultimosUsuarios);

        model.addAttribute("totalUsuarios", totalUsuarios);
        model.addAttribute("totalActivos", totalActivos);
        model.addAttribute("totalPendientes", totalPendientes);
        model.addAttribute("totalRRHH", totalRRHH);
        model.addAttribute("totalCandidatos", totalCandidatos);
        model.addAttribute("totalAdmins", totalAdmins);
        model.addAttribute("actividadReciente", actividadReciente);
        model.addAttribute("ultimosUsuarios", ultimosUsuariosData);
        model.addAttribute("viewMode", "dashboard");

        return "admin";
    }

    private List<Map<String, Object>> buildActividadReciente(List<Usuario> usuarios) {
        List<Map<String, Object>> actividades = new ArrayList<>();
        String[] colores = { "#0D9488", "#0EA5E9", "#8B5CF6", "#F59E0B", "#EF4444" };
        int idx = 0;
        for (Usuario u : usuarios) {
            Map<String, Object> act = new HashMap<>();
            String nombre = obtenerNombreUsuario(u.getId(), u.getRol());
            String iniciales = obtenerIniciales(u.getId(), u.getRol());
            act.put("titulo", "Nuevo usuario registrado");
            act.put("usuario", nombre);
            act.put("fecha", "Reci\u00e9n registrado");
            act.put("tipo", u.getRol().replace("ROLE_", ""));
            act.put("iniciales", iniciales);
            act.put("colorAvatar", colores[idx % colores.length]);
            actividades.add(act);
            idx++;
        }
        return actividades;
    }

    private List<Map<String, Object>> buildUltimosUsuariosData(List<Usuario> usuarios) {
        List<Map<String, Object>> lista = new ArrayList<>();
        for (Usuario u : usuarios) {
            Map<String, Object> map = new HashMap<>();
            String nombre = obtenerNombreUsuario(u.getId(), u.getRol());
            String[] parts = nombre.split(" ", 2);
            map.put("id", u.getId());
            map.put("username", parts.length > 0 ? parts[0] : "");
            map.put("apellido", parts.length > 1 ? parts[1] : "");
            map.put("email", u.getEmail());
            map.put("rol", u.getRol());
            map.put("activo", u.isActivo());
            map.put("fechaRegistro", "Reci\u00e9n");
            lista.add(map);
        }
        return lista;
    }

    @GetMapping
    public String panelAdmin(Model model,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Usuario> usuariosPage = usuarioRepository.findAll(pageable);

        long totalItems = usuariosPage.getTotalElements();
        int totalPages = usuariosPage.getTotalPages();
        int startItem = totalItems == 0 ? 0 : page * size + 1;
        int endItem = (int) Math.min((long) page * size + usuariosPage.getNumberOfElements(), totalItems);

        long totalUsuarios = usuarioRepository.count();
        long totalRRHH = usuarioRepository.countByRol("ROLE_RRHH");
        long totalCandidatos = usuarioRepository.countByRol("ROLE_CANDIDATO");
        long totalAdmins = usuarioRepository.countByRol("ROLE_ADMINISTRADOR");

        List<Map<String, Object>> usuariosData = usuariosPage.getContent().stream().map(u -> {
            Map<String, Object> m = new HashMap<>();
            String nombre = obtenerNombreUsuario(u.getId(), u.getRol());
            String[] parts = nombre.split(" ", 2);
            m.put("id", u.getId());
            m.put("username", parts.length > 0 ? parts[0] : "");
            m.put("apellido", parts.length > 1 ? parts[1] : "");
            m.put("email", u.getEmail());
            m.put("rol", u.getRol());
            m.put("activo", u.isActivo());
            return m;
        }).collect(Collectors.toList());

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

        return "admin";
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
                            @RequestParam String apellido) {
        nuevoRRHH.setRol("ROLE_RRHH");

        String respuesta = usuarioService.registrarUsuario(nuevoRRHH, username, apellido);

        if ("DUPLICADO".equals(respuesta)) {
            return "redirect:/admin?error=duplicado";
        }
        return "redirect:/admin?pendiente";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id) {
        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        if (usuario != null) {
            if ("ROLE_CANDIDATO".equals(usuario.getRol())) {
                candidatoRepository.deleteById(id);
            } else if ("ROLE_RRHH".equals(usuario.getRol())) {
                rrhhRepository.deleteById(id);
            }
            usuarioRepository.delete(usuario);
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

    private String obtenerIniciales(Long userId, String rol) {
        if (rol == null) return "US";
        return switch (rol) {
            case "ROLE_CANDIDATO" -> candidatoRepository.findById(userId)
                .map(c -> (c.getUsername().charAt(0) + "" + c.getApellido().charAt(0)).toUpperCase())
                .orElse("CA");
            case "ROLE_RRHH" -> rrhhRepository.findById(userId)
                .map(r -> (r.getUsername().charAt(0) + "" + r.getApellido().charAt(0)).toUpperCase())
                .orElse("RH");
            default -> "US";
        };
    }
}
