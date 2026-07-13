package com.back.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.back.model.Usuario;
import com.back.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmailService emailService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public String registrarUsuario(Usuario usuario) {

        logger.info("Iniciando registro de usuario: {}", usuario.getEmail());


        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            logger.warn("Correo duplicado: {}", usuario.getEmail());
            return "DUPLICADO";
        }

        usuario.setClave(encoder.encode(usuario.getClave()));

        if (usuario.getRol() == null || usuario.getRol().isEmpty()) {
            usuario.setRol("ROLE_CANDIDATO");
        }

        String token = UUID.randomUUID().toString();
        usuario.setTokenactivacion(token);
        usuario.setActivo(false);
        usuario.setFechaCreacionToken(LocalDateTime.now());
        usuarioRepository.save(usuario);

        usuarioRepository.save(usuario);
        logger.info("Usuario guardado en BD: {}", usuario.getEmail());

        logger.info("Intentando enviar email de verificación a: {}", usuario.getEmail());

        boolean emailSent = emailService.enviarEmailVerificacion(
                usuario.getEmail(),
                usuario.getUsername(),
                token);

        if (emailSent) {
            logger.info("Email enviado correctamente");
        } else {
            logger.warn("Email no pudo ser enviado, pero el usuario se registró");
        }

        return "EXITOSO";
    }

    public Usuario buscarPorToken(String token) {
        return usuarioRepository.findByTokenactivacion(token).orElse(null);
    }

    public void regenerarYReenviarToken(String tokenViejo) {
        Usuario usuario = usuarioRepository.findByTokenactivacion(tokenViejo).orElse(null);
        
        if (usuario != null) {
            String nuevoToken = UUID.randomUUID().toString();
            usuario.setTokenactivacion(nuevoToken);
            usuario.setFechaCreacionToken(LocalDateTime.now());
            usuarioRepository.save(usuario);
            
            emailService.enviarEmailVerificacion(usuario.getEmail(), usuario.getUsername(), nuevoToken);
        }
    }

    public boolean activarCuenta(String token) {

        logger.info("Buscando token de activación: {}", token);

        var optional = usuarioRepository.findByTokenactivacion(token);

        if (optional.isEmpty()) {
            logger.warn("Token no encontrado o inválido");
            return false;
        }

        Usuario usuario = optional.get();
        usuario.setActivo(true);
        usuario.setTokenactivacion(null);
        usuarioRepository.save(usuario);

        logger.info("Cuenta activada para: {}", usuario.getEmail());

        return true;
    }

    public void generarTokenRecuperacion(String email) {

        var optional = usuarioRepository.findByEmail(email);

        if (optional.isEmpty()) {
            return;
        }

        Usuario usuario = optional.get();

        String token = UUID.randomUUID().toString();
        usuario.setTokenactivacion(token);

        if (usuario.getApellido() == null || usuario.getApellido().isBlank()) {
            usuario.setApellido("N/A");
        }

        usuarioRepository.save(usuario);

        emailService.enviarEmailRecuperacion(
                email,
                usuario.getUsername(),
                token);
    }

    public boolean cambiarPassword(String token, String nuevaPassword) {

        var optional = usuarioRepository.findByTokenactivacion(token);

        if (optional.isEmpty()) {
            return false;
        }

        Usuario usuario = optional.get();

        usuario.setClave(encoder.encode(nuevaPassword));
        usuario.setTokenactivacion(null);

        usuarioRepository.save(usuario);

        return true;
    }
}