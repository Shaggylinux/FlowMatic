package com.back.service;


import com.model.Usuario;
import com.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioService usuarioService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public String registrarUsuario(Usuario usuario){
        if (UsuarioRepository.findByCorreo(usuario.getCorreo()).isPresent()){
            return "DUPLICADO";
        }
        usuario.setContrasena(encoder.encode(usuario.getContrasena()));
        UsuarioRepository.save(usuario);
        return "Exitoso"
    }
}
