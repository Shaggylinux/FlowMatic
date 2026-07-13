package com.back.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {
    @GetMapping("/login")
    public String Login(){
        return "login";
    }

    @GetMapping("/post-login")
    public String redirigirTrasLogin(org.springframework.security.core.Authentication auth) {
        
        var roles = auth.getAuthorities().stream()
                        .map(r -> r.getAuthority())
                        .toList();

        if (roles.contains("ROLE_CANDIDATO") || roles.contains("ROLE_RRHH")) {
            return "redirect:/drive";
        }

        if (roles.contains("ROLE_ADMINISTRADOR")) {
            return "redirect:/admin/dashboard";
        }
        return "redirect:/";
    }

    @GetMapping("/candidato/home")
    public String vistaCandidato() {
        return "candidato";
    }
}