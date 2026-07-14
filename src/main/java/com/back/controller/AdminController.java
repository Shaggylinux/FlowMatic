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
import com.back.repository.UsuarioRepository;
import com.back.service.ExcelService;
import com.back.service.UsuarioService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UsuarioRepository usuarioRepository;

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
        long totalAdmins = usuarioRepository.countByRol("ROLE_ADMIN");

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
            act.put("titulo", "Nuevo usuario registrado");
            act.put("usuario", u.getUsername() + " " + u.getApellido());
            act.put("fecha", "Recién registrado");
            act.put("tipo", u.getRol().replace("ROLE_", ""));
            act.put("iniciales", (u.getUsername().charAt(0) + "" + u.getApellido().charAt(0)).toUpperCase());
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
            map.put("id", u.getId());
            map.put("username", u.getUsername());
            map.put("apellido", u.getApellido());
            map.put("email", u.getEmail());
            map.put("rol", u.getRol());
            map.put("activo", u.isActivo());
            map.put("fechaRegistro", "Recién");
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
        long totalAdmins = usuarioRepository.countByRol("ROLE_ADMIN");

        model.addAttribute("usuarios", usuariosPage.getContent());
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
    public String crearRRHH(@ModelAttribute Usuario nuevoRRHH) {
        nuevoRRHH.setRol("ROLE_RRHH");

        String respuesta = usuarioService.registrarUsuario(nuevoRRHH);

        if ("DUPLICADO".equals(respuesta)) {
            return "redirect:/admin?error=duplicado";
        }
        return "redirect:/admin?pendiente";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id) {
        usuarioRepository.deleteById(id);
        return "redirect:/admin";
    }

    @PostMapping("/editar")
    public String editarUsuario(@ModelAttribute Usuario datosEditados,
            @RequestParam(value = "nuevaClave", required = false) String nuevaClave) {

        Usuario usuarioBD = usuarioRepository.findById(datosEditados.getId()).orElse(null);

        if (usuarioBD != null) {
            usuarioBD.setUsername(datosEditados.getUsername());
            usuarioBD.setApellido(datosEditados.getApellido());
            usuarioBD.setEmail(datosEditados.getEmail());

            if (nuevaClave != null && !nuevaClave.trim().isEmpty()) {
                System.out.println("Detectada nueva clave para: " + usuarioBD.getUsername());

                String claveEncriptada = passwordEncoder.encode(nuevaClave);
                usuarioBD.setClave(claveEncriptada);

                System.out.println("Clave encriptada y seteada correctamente.");
            } else {
                System.out.println("No se envió nueva clave, se mantiene la anterior.");
            }
            usuarioRepository.save(usuarioBD);
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
}