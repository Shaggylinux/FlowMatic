package com.back.auth;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final UsuarioService usuarioService;

    @GetMapping("/")
    public String Home(){
        return "home";
    }

    @GetMapping({"/activar-cuenta", "/activar"})
    public String activarCuentaGlobal(@RequestParam(name = "token", required = false) String token, Model model) {
        if (token == null || token.isBlank()) {
            model.addAttribute("tokenInvalido", true);
            model.addAttribute("enlaceExpirado", true);
            return "caduco";
        }
        boolean activado = usuarioService.activarCuenta(token.trim());
        if (!activado) {
            model.addAttribute("tokenInvalido", true);
            model.addAttribute("enlaceExpirado", true);
            return "caduco";
        }
        model.addAttribute("activacionExitosa", true);
        return "activacion";
    }
}
