package com.back.controller;

import com.back.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class PasswordController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/forgot-password")
    public String mostrarFormulario() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String procesarFormulario(@RequestParam String email) {
        usuarioService.generarTokenRecuperacion(email);
        return "redirect:/login?resetEnviado";
    }

    @GetMapping("/reset-password")
    public String mostrarReset(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String cambiarPassword(@RequestParam String token,
            @RequestParam String password) {

        boolean ok = usuarioService.cambiarPassword(token, password);

        if (!ok) {
            return "redirect:/login?errorToken";
        }

        return "redirect:/login?passwordCambiada";
    }
}