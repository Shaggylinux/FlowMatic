package com.back.web;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.back.auth.Usuario;
import com.back.auth.UsuarioService;

import lombok.RequiredArgsConstructor;

@ControllerAdvice
@RequiredArgsConstructor
public class AtributosUsuarioAdvice {

    private final UsuarioService usuarioService;

    @ModelAttribute
    public void agregarUsuarioActual(Model model) {
        if (model.containsAttribute("usuarioActualObjeto")) {
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return;
        }

        String email = auth.getName();
        if (email == null || email.isBlank()) {
            return;
        }

        Usuario usuario = usuarioService.buscarPorEmail(email).orElse(null);
        if (usuario == null) {
            return;
        }

        Map<String, Object> usuarioData = new HashMap<>();
        usuarioData.put("id", usuario.getId());
        usuarioData.put("email", usuario.getEmail());
        usuarioData.put("rol", usuario.getRol());
        usuarioData.put("activo", usuario.isActivo());

        model.addAttribute("usuarioActualObjeto", usuarioData);
        model.addAttribute("usuarioActual", email);
    }
}