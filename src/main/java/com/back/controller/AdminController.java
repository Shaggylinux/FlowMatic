package com.back.controller;

import com.back.repository.UsuarioRepository;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
import com.back.service.ExcelService;
import com.back.service.UsuarioService;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UsuarioService usuarioService;

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

        model.addAttribute("usuarios", usuariosPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("pageSize", usuariosPage.getSize());
        model.addAttribute("startItem", startItem);
        model.addAttribute("endItem", endItem);
        model.addAttribute("pageItems", getPageItems(page, totalPages));

        return "admin";
    }

    private List<PageItem> getPageItems(int current, int total) {
        List<PageItem> items = new ArrayList<>();
        if (total <= 5) {
            for (int i = 0; i < total; i++) items.add(new PageItem(i, false));
            return items;
        }
        items.add(new PageItem(0, false));
        if (current > 2) items.add(new PageItem(-1, true));
        int start = Math.max(1, current - 1);
        int end = Math.min(total - 2, current + 1);
        if (current <= 2) end = Math.min(3, total - 2);
        if (current >= total - 3) start = Math.max(total - 4, 1);
        for (int i = start; i <= end; i++) items.add(new PageItem(i, false));
        if (current < total - 3) items.add(new PageItem(-1, true));
        items.add(new PageItem(total - 1, false));
        return items;
    }

    public record PageItem(int number, boolean ellipsis) {}

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

    @Autowired
    private ExcelService excelService;

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