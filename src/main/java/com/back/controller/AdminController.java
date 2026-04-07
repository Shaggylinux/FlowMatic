package com.back.controller;

import com.back.repository.UsuarioRepository;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.List;

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
    public String panelAdmin(Model model) {
        model.addAttribute("usuarios", usuarioRepository.findAll());
        return "admin";
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
            usuarioBD.setTelefono(datosEditados.getTelefono());
            
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