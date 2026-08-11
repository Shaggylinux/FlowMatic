package com.back.auth;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String Login(){
        return "login";
    }

    @GetMapping("/post-login")
    public String redirigirTrasLogin(Authentication auth) {
        
        var roles = auth.getAuthorities().stream()
                        .map(r -> r.getAuthority())
                        .toList();

        if (roles.contains("ROLE_RRHH")) {
            return "redirect:/dashboard";
        }

        if (roles.contains("ROLE_CANDIDATO")) {
            return "redirect:/candidato/home";
        }

        if (roles.contains("ROLE_ADMINISTRADOR")) {
            return "redirect:/admin/dashboard";
        }
        return "redirect:/";
    }
}
