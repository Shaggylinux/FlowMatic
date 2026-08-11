package com.back.auth;

import com.back.shared.api.ConfiguracionApi;
import com.back.auth.UsuarioService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class PasswordController {

    private final UsuarioService usuarioService;
    private final ConfiguracionApi configuracionService;

    @GetMapping("/forgot-password")
    public String mostrarFormulario() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String procesarFormulario(@RequestParam String email) {
        usuarioService.generarTokenRecuperacion(email);
        return "redirect:/forgot-password?success";
    }

    @GetMapping("/reset-password")
    public String mostrarReset(@RequestParam(required = false) String token,
            @RequestParam(required = false) String success,
            Model model) {
        if (success != null) {
            return "reset-password";
        }

        if (token == null || token.isEmpty()) {
            return "redirect:/forgot-password?errorToken";
        }

        String estado = usuarioService.validarTokenReset(token);
        if ("INVALIDO".equals(estado)) {
            return "redirect:/forgot-password?errorToken";
        }
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String cambiarPassword(@RequestParam String token,
            @RequestParam String password,
            Model model) {

        int minLength = Integer.parseInt(configuracionService.getValor("password.min.length", "8"));
        if (password == null || password.trim().length() < minLength) {
            model.addAttribute("token", token);
            model.addAttribute("errorPassword", true);
            return "reset-password";
        }

        String estado = usuarioService.validarTokenReset(token);
        if ("INVALIDO".equals(estado)) {
            return "redirect:/forgot-password?errorToken";
        }

        boolean ok = usuarioService.cambiarPassword(token, password);

        if (!ok) {
            return "redirect:/forgot-password?errorToken";
        }

        return "redirect:/login?reset_ok";
    }
}