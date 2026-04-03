package com.back.controller;

import com.back.model.Usuario;
import com.back.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/registro/rrhh")
public class RegistroRRHHController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public String mostrarFormulario(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro-rrhh";
    }

    @PostMapping
    public String procesarRegistro(
            @Valid @ModelAttribute("usuario") Usuario usuario,
            BindingResult resultado,
            Model model) {

        // Rol para RRHH (puedes cambiarlo si usas otro)
        usuario.setRol("ROLE_ADMIN");

        if (resultado.hasErrors()) {
            return "registro-rrhh";
        }

        String respuesta = usuarioService.registrarUsuario(usuario);

        if ("DUPLICADO".equals(respuesta)) {
            model.addAttribute("errorDuplicado", true);
            return "registro-rrhh";
        }

        return "redirect:/registro/rrhh?pendiente";
    }

    @GetMapping(params = "pendiente")
    public String registropendiente(Model model){
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("mensajePendiente", true);
        return "registro-rrhh";
    }
}