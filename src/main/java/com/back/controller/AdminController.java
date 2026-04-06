package com.back.controller;

import com.back.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.ui.Model;
import com.back.model.Usuario;
import com.back.service.UsuarioService;;

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
}