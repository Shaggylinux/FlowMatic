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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.ui.Model;
import com.back.auth.Usuario;
import com.back.candidatos.Candidato;
import com.back.auth.UsuarioRepository;
import com.back.candidatos.CandidatoRepository;
import com.back.shared.ExcelService;
import com.back.auth.UsuarioService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import com.back.admin.dto.ActividadRecienteDTO;
import com.back.admin.dto.UsuarioResumenDTO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

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
    private final AdminService adminService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        long totalUsuarios = usuarioRepository.count();
        long totalActivos = usuarioRepository.countByActivoTrue();
        long totalPendientes = usuarioRepository.countByActivoFalse();
        long totalRRHH = usuarioRepository.countByRol("ROLE_RRHH");
        long totalCandidatos = usuarioRepository.countByRol("ROLE_CANDIDATO");
        long totalAdmins = usuarioRepository.countByRol("ROLE_ADMINISTRADOR");

        List<Usuario> ultimosUsuarios = usuarioRepository.findTop10ByOrderByIdDesc();
        List<ActividadRecienteDTO> actividadReciente = adminService.buildActividadReciente(ultimosUsuarios);
        List<UsuarioResumenDTO> ultimosUsuariosData = adminService.buildUltimosUsuariosData(ultimosUsuarios);

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
}
