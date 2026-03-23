package com.back.service;

import com.back.model.Usuario;
import com.back.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public String registrarUsuario(Usuario usuario) {
        if (usuarioRepository.findByCorreo(usuario.getCorreo()).isPresent()) {
            return "DUPLICADO";
        }
        usuario.setContrasena(encoder.encode(usuario.getContrasena()));
        usuarioRepository.save(usuario);
        return "EXITOSO";
    }
}