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
@RequestMapping("/registro")
public class RegistroController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public String mostrarFormulario(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro";
    }

    @PostMapping
    public String procesarRegistro(
            @Valid @ModelAttribute("usuario") Usuario usuario,
            BindingResult resultado,
            Model model) {

        if (usuario.getRol() == null || usuario.getRol().isEmpty()){
            usuario.setRol("ROLE_USER");
        }

        if (resultado.hasErrors()) {
            return "registro";
        }

        String respuesta = usuarioService.registrarUsuario(usuario);

        if ("DUPLICADO".equals(respuesta)) {
            model.addAttribute("errorDuplicado", true);
            return "registro";
        }

        // He dejado la redirección a 'pendiente' porque parece ser el flujo nuevo
        return "redirect:/registro?pendiente";
    }

    @GetMapping(params = "pendiente")
    public String registropendiente(Model model){
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("mensajePendiente", true);
        return "registro";
    }

    @GetMapping("/activar")
    public String activarCuenta(
            @RequestParam("token") String token,
            Model model
    ){
        boolean activado = usuarioService.activarCuenta(token);

        if (activado){
            model.addAttribute("activacionExitosa", true);
        } else {
            model.addAttribute("tokenInvalido", true);
        }
        return "activacion";
    }

    @PostMapping("/verificar")
    public String verificarSms(
            @RequestParam("token") String token,
            Model model
    ){
        boolean activado = usuarioService.activarCuenta(token);

        if (activado){
            model.addAttribute("activacionExitosa", true);
            return "activacion";
        } else {
            model.addAttribute("mensajePendiente", true);
            model.addAttribute("errorVerificacion", true);
            model.addAttribute("usuario", new Usuario());
            return "registro";
        }
    }
}