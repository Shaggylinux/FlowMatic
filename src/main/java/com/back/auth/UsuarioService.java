package com.back.auth;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.back.auth.event.PasswordResetSolicitadoEvent;
import com.back.auth.event.UsuarioRegistradoEvent;
import com.back.shared.api.AuthApi;
import com.back.shared.api.ConfiguracionApi;
import com.back.shared.dto.RegistroUsuarioDTO;
import org.springframework.context.ApplicationEventPublisher;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService implements AuthApi {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioRepository usuarioRepository;

    private final ConfiguracionApi configuracionService;
    private final ApplicationEventPublisher eventPublisher;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String registrarUsuario(RegistroUsuarioDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setEmail(dto.getEmail());
        usuario.setClave(dto.getClave());
        usuario.setRol(dto.getRol());
        
        String username = dto.getUsername();
        String apellido = dto.getApellido();
        String telefono = dto.getTelefono();

        logger.info("Iniciando registro de usuario: {}", usuario.getEmail());

        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            logger.warn("Correo duplicado: {}", usuario.getEmail());
            return "DUPLICADO";
        }

        int minLength = Integer.parseInt(configuracionService.getValor("password.min.length", "8"));
        if (usuario.getClave() == null || usuario.getClave().trim().length() < minLength) {
            logger.warn("Contrase\u00f1a demasiado corta: {}", usuario.getEmail());
            return "CLAVE_CORTA";
        }

        usuario.setClave(encoder.encode(usuario.getClave()));

        if (usuario.getRol() == null || usuario.getRol().isEmpty()) {
            usuario.setRol("ROLE_CANDIDATO");
        }

        String token = UUID.randomUUID().toString();
        usuario.setTokenActivacion(token);
        usuario.setActivo(false);
        usuario.setFechaCreacionToken(LocalDateTime.now());
        usuario = usuarioRepository.save(usuario);

        eventPublisher.publishEvent(new UsuarioRegistradoEvent(this, usuario.getId(),
            usuario.getEmail(), usuario.getRol(), username, apellido, telefono, token));

        return "EXITOSO";
    }

    public Usuario buscarPorToken(String token) {
        return usuarioRepository.findByTokenActivacion(token).orElse(null);
    }

    public Usuario buscarPorTokenReset(String token) {
        return usuarioRepository.findByTokenResetPassword(token).orElse(null);
    }

    public void regenerarYReenviarToken(String tokenViejo) {
        Usuario usuario = usuarioRepository.findByTokenActivacion(tokenViejo).orElse(null);

        if (usuario != null) {
            String nuevoToken = UUID.randomUUID().toString();
            usuario.setTokenActivacion(nuevoToken);
            usuario.setFechaCreacionToken(LocalDateTime.now());
            usuarioRepository.save(usuario);

            String nombre = obtenerNombreOApellido(usuario);
            eventPublisher.publishEvent(new UsuarioRegistradoEvent(this, usuario.getId(),
                usuario.getEmail(), usuario.getRol(), nombre, null, null, nuevoToken));
        }
    }

    public boolean activarCuenta(String token) {
        logger.info("Buscando token de activaci\u00f3n: {}", token);

        var optional = usuarioRepository.findByTokenActivacion(token);

        if (optional.isEmpty()) {
            logger.warn("Token no encontrado o inv\u00e1lido");
            return false;
        }

        Usuario usuario = optional.get();
        usuario.setActivo(true);
        usuario.setTokenActivacion(null);
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
        usuario.setTokenResetPassword(token);
        usuario.setFechaCreacionTokenReset(LocalDateTime.now());
        usuarioRepository.save(usuario);

        String nombre = obtenerNombreOApellido(usuario);
        eventPublisher.publishEvent(new PasswordResetSolicitadoEvent(this, email, nombre, token));
    }

    public String validarTokenReset(String token) {
        var optional = usuarioRepository.findByTokenResetPassword(token);
        if (optional.isEmpty()) return "INVALIDO";
        Usuario usuario = optional.get();
        if (usuario.getFechaCreacionTokenReset() != null) {
            long minutos = java.time.Duration.between(usuario.getFechaCreacionTokenReset(), LocalDateTime.now()).toMinutes();
            long expiry = Long.parseLong(configuracionService.getValor("password.reset.expiry.minutes", "15"));
            if (minutos > expiry) return "EXPIRADO";
        }
        return "VALIDA";
    }

public boolean cambiarPassword(String token, String nuevaPassword) {
        var optional = usuarioRepository.findByTokenResetPassword(token);

        if (optional.isEmpty()) {
            return false;
        }

        Usuario usuario = optional.get();

        if (usuario.getFechaCreacionTokenReset() != null) {
            long minutos = java.time.Duration.between(usuario.getFechaCreacionTokenReset(), LocalDateTime.now()).toMinutes();
            long expiry = Long.parseLong(configuracionService.getValor("password.reset.expiry.minutes", "15"));
            if (minutos > expiry) {
                usuario.setTokenResetPassword(null);
                usuarioRepository.save(usuario);
                return false;
            }
        }

        usuario.setClave(encoder.encode(nuevaPassword));
        usuario.setTokenResetPassword(null);

        usuarioRepository.save(usuario);

        return true;
    }

    private String obtenerNombreOApellido(Usuario usuario) {
        String email = usuario.getEmail();
        if (email != null && email.contains("@")) {
            return email.substring(0, email.indexOf("@"));
        }
        return "Usuario";
    }
}
