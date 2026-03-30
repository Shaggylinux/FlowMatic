package com.back.service;

import com.back.model.Usuario;
import com.back.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UsuarioService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmailService emailService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public String registrarUsuario(Usuario usuario) {
        logger.info("Iniciando registro de usuario: {}", usuario.getCorreo());
        
        if (usuarioRepository.findByCorreo(usuario.getCorreo()).isPresent()) {
            logger.warn("⚠Correo duplicado: {}", usuario.getCorreo());
            return "DUPLICADO";
        }

        usuario.setClave(encoder.encode(usuario.getClave()));

        if (usuario.getRol() == null || usuario.getRol().isEmpty()) {
            usuario.setRol("ROLE_USER");
        }

        String token = UUID.randomUUID().toString();
        usuario.setTokenActivacion(token);
        usuario.setActivo(false);

        usuarioRepository.save(usuario);
        logger.info("✅ Usuario guardado en BD: {}", usuario.getCorreo());

        logger.info("📨 Intentando enviar email de verificación a: {}", usuario.getCorreo());
        
        boolean emailSent = emailService.enviarEmailVerificacion(usuario.getCorreo(), usuario.getUsername(), token);
        
        if (emailSent) {
            logger.info("Email enviado correctamente");
            return "EXITOSO";
        } else {
            logger.warn("Email no pudo ser enviado, pero el usuario se registró");
            return "EXITOSO";
        }
    }

    public boolean activarCuenta(String token) {
        logger.info("Buscando token de activación: {}", token);
        
        var optional = usuarioRepository.findByTokenActivacion(token);

        if (optional.isEmpty()) {
            logger.warn("Token no encontrado o inválido");
            return false;
        }

        Usuario usuario = optional.get();
        usuario.setActivo(true);
        usuario.setTokenActivacion(null);
        usuarioRepository.save(usuario);
        
        logger.info("Cuenta activada para: {}", usuario.getCorreo());

        return true;
    }
}

